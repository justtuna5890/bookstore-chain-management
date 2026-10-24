package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.PromotionRequestDTO;
import com.bookstorechain.dto.response.PromotionDTO;
import com.bookstorechain.entity.Promotion;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.PromotionType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.PromotionRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.service.AdminPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminPromotionServiceImpl implements AdminPromotionService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PromotionRepository promotionRepository;
    private final StoreRepository storeRepository;
    private final PromotionMapper promotionMapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionDTO> list(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now(clock);
        return promotionRepository.findAll(pageable).map(p -> promotionMapper.toDTO(p, now));
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionDTO get(Long id) {
        return promotionMapper.toDTO(find(id), LocalDateTime.now(clock));
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionRequestDTO toRequest(Long id) {
        Promotion p = find(id);
        return PromotionRequestDTO.builder()
                .code(p.getCode())
                .description(p.getDescription())
                .type(p.getType())
                .value(plain(p.getValue()))
                .minOrderAmount(plain(p.getMinOrderAmount()))
                .maxDiscountAmount(plain(p.getMaxDiscountAmount()))
                .startsAt(p.getStartsAt())
                .endsAt(p.getEndsAt())
                .usageLimit(p.getUsageLimit())
                .storeId(p.getStore() == null ? null : p.getStore().getId())
                .onePerCustomer(p.isOnePerCustomer())
                .active(p.isActive())
                .build();
    }

    @Override
    @Transactional
    public PromotionDTO create(PromotionRequestDTO request) {
        String code = PromotionServiceImpl.normalize(request.getCode());
        if (code.isEmpty()) {
            throw new BusinessException("Vui lòng nhập mã giảm giá.");
        }
        if (promotionRepository.existsByCode(code)) {
            throw new BusinessException("Mã giảm giá " + code + " đã tồn tại.");
        }
        Promotion promotion = new Promotion();
        promotion.setCode(code);
        apply(promotion, request, 0);
        return promotionMapper.toDTO(promotionRepository.save(promotion), LocalDateTime.now(clock));
    }

    @Override
    @Transactional
    public PromotionDTO update(Long id, PromotionRequestDTO request) {
        Promotion promotion = find(id);
        apply(promotion, request, promotion.getUsedCount());
        return promotionMapper.toDTO(promotionRepository.save(promotion), LocalDateTime.now(clock));
    }

    @Override
    @Transactional
    public PromotionDTO setActive(Long id, boolean active) {
        Promotion promotion = find(id);
        promotion.setActive(active);
        return promotionMapper.toDTO(promotionRepository.save(promotion), LocalDateTime.now(clock));
    }

    /** Bỏ phần thập phân thừa (5.00 thành 5) để ô nhập hiển thị gọn. */
    private static BigDecimal plain(BigDecimal value) {
        return value == null ? null : new BigDecimal(value.stripTrailingZeros().toPlainString());
    }

    private Promotion find(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + id));
    }

    /** Kiểm tra các quy tắc liên trường rồi ghi dữ liệu form vào mã (không đổi chuỗi mã). */
    private void apply(Promotion promotion, PromotionRequestDTO r, int usedCount) {
        if (r.getType() == PromotionType.PERCENT && r.getValue().compareTo(HUNDRED) > 0) {
            throw new BusinessException("Giảm theo phần trăm không được vượt quá 100%.");
        }
        if (r.getType() == PromotionType.FIXED && r.getValue().compareTo(BigDecimal.ONE) < 0) {
            throw new BusinessException("Số tiền giảm phải từ 1 đồng trở lên.");
        }
        if (!r.getEndsAt().isAfter(r.getStartsAt())) {
            throw new BusinessException("Thời điểm kết thúc phải sau thời điểm bắt đầu.");
        }
        if (r.getUsageLimit() != null && r.getUsageLimit() < usedCount) {
            throw new BusinessException("Giới hạn lượt không được nhỏ hơn số lượt đã dùng (" + usedCount + ").");
        }
        Store store = null;
        if (r.getStoreId() != null) {
            store = storeRepository.findById(r.getStoreId())
                    .orElseThrow(() -> new BusinessException("Chi nhánh được chọn không tồn tại."));
        }

        promotion.setDescription(r.getDescription() == null || r.getDescription().isBlank() ? null : r.getDescription().trim());
        promotion.setType(r.getType());
        promotion.setValue(r.getValue().setScale(r.getType() == PromotionType.PERCENT ? 2 : 0, RoundingMode.HALF_UP));
        promotion.setMinOrderAmount(r.getMinOrderAmount() == null ? BigDecimal.ZERO : r.getMinOrderAmount().setScale(0, RoundingMode.DOWN));
        // Mức giảm tối đa chỉ có nghĩa với giảm theo phần trăm
        promotion.setMaxDiscountAmount(r.getType() == PromotionType.PERCENT && r.getMaxDiscountAmount() != null
                ? r.getMaxDiscountAmount().setScale(0, RoundingMode.DOWN) : null);
        promotion.setStartsAt(r.getStartsAt());
        promotion.setEndsAt(r.getEndsAt());
        promotion.setUsageLimit(r.getUsageLimit());
        promotion.setStore(store);
        promotion.setOnePerCustomer(r.isOnePerCustomer());
        promotion.setActive(r.isActive());
    }
}
