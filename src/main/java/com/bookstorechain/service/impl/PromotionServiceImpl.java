package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.PromotionApplicationDTO;
import com.bookstorechain.dto.response.PromotionDTO;
import com.bookstorechain.entity.Promotion;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PromotionType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.PromotionRepository;
import com.bookstorechain.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PromotionRepository promotionRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final PromotionMapper promotionMapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public PromotionApplicationDTO evaluate(String code, Long userId, Map<Long, BigDecimal> subtotalByStoreId) {
        return validateAndCompute(code, userId, subtotalByStoreId);
    }

    @Override
    @Transactional
    public PromotionApplicationDTO redeem(String code, Long userId, Map<Long, BigDecimal> subtotalByStoreId) {
        PromotionApplicationDTO application = validateAndCompute(code, userId, subtotalByStoreId);
        // Lượt dùng tính theo số đơn nhận giảm giá; UPDATE có điều kiện để hai khách không cùng lấy lượt cuối
        if (promotionRepository.consume(application.getPromotionId(), application.getDiscountByStoreId().size()) == 0) {
            throw new BusinessException("Mã giảm giá vừa hết lượt sử dụng.");
        }
        return application;
    }

    @Override
    @Transactional
    public void releaseUsage(Long promotionId) {
        promotionRepository.release(promotionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionDTO> getAvailablePromotions(Set<Long> storeIds) {
        LocalDateTime now = LocalDateTime.now(clock);
        return promotionRepository.findRunning(now).stream()
                .filter(p -> p.getStore() == null || storeIds.contains(p.getStore().getId()))
                .map(p -> promotionMapper.toDTO(p, now))
                .collect(Collectors.toList());
    }

    private PromotionApplicationDTO validateAndCompute(String rawCode, Long userId, Map<Long, BigDecimal> subtotalByStoreId) {
        String code = normalize(rawCode);
        if (code.isEmpty()) {
            throw new BusinessException("Vui lòng nhập mã giảm giá.");
        }
        if (subtotalByStoreId == null || subtotalByStoreId.isEmpty()) {
            throw new BusinessException("Giỏ hàng chưa có sách khả dụng để áp mã giảm giá.");
        }
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại."));

        LocalDateTime now = LocalDateTime.now(clock);
        if (!promotion.isActive()) {
            throw new BusinessException("Mã giảm giá đang tạm dừng.");
        }
        if (now.isBefore(promotion.getStartsAt())) {
            throw new BusinessException("Mã giảm giá chưa đến thời gian áp dụng (từ " + promotion.getStartsAt().format(DATE_TIME) + ").");
        }
        if (now.isAfter(promotion.getEndsAt())) {
            throw new BusinessException("Mã giảm giá đã hết hạn.");
        }
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new BusinessException("Mã giảm giá đã hết lượt sử dụng.");
        }

        // Chỉ các đơn thuộc chi nhánh được áp mã mới tham gia; sắp theo id để chia phần giảm ổn định
        Map<Long, BigDecimal> eligible = new TreeMap<>();
        subtotalByStoreId.forEach((storeId, subtotal) -> {
            if (promotion.getStore() == null || promotion.getStore().getId().equals(storeId)) {
                eligible.put(storeId, subtotal);
            }
        });
        if (eligible.isEmpty()) {
            throw new BusinessException("Mã giảm giá chỉ áp dụng cho đơn hàng tại " + promotion.getStore().getName() + ".");
        }

        BigDecimal eligibleSubtotal = eligible.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (eligibleSubtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new BusinessException("Cần đơn hàng từ " + money(promotion.getMinOrderAmount())
                    + " để dùng mã này (hiện là " + money(eligibleSubtotal) + ").");
        }
        if (promotion.isOnePerCustomer()
                && customerOrderRepository.existsByCustomerIdAndPromotionIdAndStatusNot(userId, promotion.getId(), OrderStatus.CANCELLED)) {
            throw new BusinessException("Bạn đã dùng mã giảm giá này cho một đơn hàng khác.");
        }
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() + eligible.size() > promotion.getUsageLimit()) {
            throw new BusinessException("Mã giảm giá không còn đủ lượt cho " + eligible.size() + " đơn hàng của bạn.");
        }

        BigDecimal totalDiscount = computeDiscount(promotion, eligibleSubtotal);
        return PromotionApplicationDTO.builder()
                .promotionId(promotion.getId())
                .code(promotion.getCode())
                .description(promotion.getDescription())
                .totalDiscount(totalDiscount)
                .discountByStoreId(allocate(totalDiscount, eligible))
                .build();
    }

    /** Giảm theo % (có trần) hoặc số tiền cố định, làm tròn xuống đồng và không vượt quá tổng tạm tính. */
    static BigDecimal computeDiscount(Promotion promotion, BigDecimal eligibleSubtotal) {
        BigDecimal discount;
        if (promotion.getType() == PromotionType.PERCENT) {
            discount = eligibleSubtotal.multiply(promotion.getValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
            if (promotion.getMaxDiscountAmount() != null) {
                discount = discount.min(promotion.getMaxDiscountAmount());
            }
        } else {
            discount = promotion.getValue();
        }
        return discount.min(eligibleSubtotal).setScale(0, RoundingMode.DOWN);
    }

    /**
     * Chia tổng giảm cho các đơn theo tỷ lệ tạm tính, làm tròn xuống từng đơn rồi dồn phần dư vào các đơn còn chỗ
     * (không đơn nào bị giảm quá tạm tính của nó), nên tổng các phần luôn đúng bằng tổng giảm.
     */
    static Map<Long, BigDecimal> allocate(BigDecimal totalDiscount, Map<Long, BigDecimal> subtotals) {
        BigDecimal sum = subtotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Long, BigDecimal> shares = new LinkedHashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> entry : subtotals.entrySet()) {
            BigDecimal share = totalDiscount.multiply(entry.getValue()).divide(sum, 0, RoundingMode.DOWN);
            shares.put(entry.getKey(), share);
            allocated = allocated.add(share);
        }
        BigDecimal remainder = totalDiscount.subtract(allocated);
        for (Map.Entry<Long, BigDecimal> entry : subtotals.entrySet()) {
            if (remainder.signum() == 0) {
                break;
            }
            BigDecimal room = entry.getValue().subtract(shares.get(entry.getKey()));
            BigDecimal extra = remainder.min(room);
            shares.merge(entry.getKey(), extra, BigDecimal::add);
            remainder = remainder.subtract(extra);
        }
        return shares;
    }

    static String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private static String money(BigDecimal amount) {
        return "₫" + String.format(Locale.US, "%,d", amount.longValue());
    }
}
