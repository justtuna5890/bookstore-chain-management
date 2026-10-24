package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.CheckoutRequestDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.dto.response.PromotionApplicationDTO;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.Cart;
import com.bookstorechain.entity.CartItem;
import com.bookstorechain.entity.CustomerOrder;
import com.bookstorechain.entity.OrderItem;
import com.bookstorechain.entity.Payment;
import com.bookstorechain.entity.Promotion;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.CartItemRepository;
import com.bookstorechain.repository.CartRepository;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.PromotionRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.OrderNotificationService;
import com.bookstorechain.service.OrderService;
import com.bookstorechain.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_SUFFIX_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookCopyRepository bookCopyRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final UserRepository userRepository;
    private final UserLocks userLocks;
    private final PlatformTransactionManager transactionManager;
    private final OrderMapper orderMapper;
    private final OrderNotificationService orderNotificationService;
    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;

    @Value("${app.shop.delivery-fee:25000}")
    private BigDecimal deliveryFee;

    @Override
    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    @Override
    public List<OrderSummaryDTO> checkout(Long userId, CheckoutRequestDTO request) {
        List<OrderSummaryDTO> placed;
        // Khoá ngoài transaction để bấm đặt hàng hai lần liên tiếp không tạo trùng đơn
        synchronized (userLocks.lockFor(userId)) {
            placed = new TransactionTemplate(transactionManager).execute(status -> doCheckout(userId, request));
        }
        // Chỉ báo nhân viên sau khi đơn đã commit, để không có thông báo cho đơn bị rollback
        placed.forEach(orderNotificationService::notifyNewOrder);
        return placed;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> getCustomerOrders(Long userId) {
        return customerOrderRepository.findByCustomerIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryDTO getCustomerOrder(Long userId, String orderCode) {
        return customerOrderRepository.findByOrderCodeAndCustomerId(orderCode, userId)
                .map(orderMapper::toSummary)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng " + orderCode));
    }

    @Override
    @Transactional
    public void cancelByCustomer(Long userId, String orderCode) {
        CustomerOrder order = customerOrderRepository.lockByOrderCodeAndCustomerId(orderCode, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng " + orderCode));
        if (order.getStatus() != OrderStatus.PENDING || order.getPayment().getStatus() != PaymentStatus.UNPAID) {
            throw new BusinessException("Đơn hàng đã được xử lý nên không thể tự huỷ. Vui lòng liên hệ chi nhánh để được hỗ trợ.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPromotion() != null) {
            promotionService.releaseUsage(order.getPromotion().getId());
        }
        for (OrderItem item : order.getItems()) {
            BookCopy copy = item.getCopy();
            if (copy.getStatus() == CopyStatus.RESERVED) {
                copy.setStatus(CopyStatus.AVAILABLE);
            }
        }
    }

    private List<OrderSummaryDTO> doCheckout(Long userId, CheckoutRequestDTO request) {
        boolean delivery = request.getFulfillmentType() == FulfillmentType.DELIVERY;
        if (delivery && isBlank(request.getShippingAddress())) {
            throw new BusinessException("Vui lòng nhập địa chỉ giao hàng.");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Giỏ hàng của bạn đang trống."));
        List<CartItem> purchasable = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId()).stream()
                .filter(item -> item.getCopy().getStatus() == CopyStatus.AVAILABLE)
                .collect(Collectors.toList());
        if (purchasable.isEmpty()) {
            throw new BusinessException("Không có cuốn sách nào còn khả dụng trong giỏ hàng.");
        }

        List<Long> copyIds = purchasable.stream().map(item -> item.getCopy().getId()).sorted().collect(Collectors.toList());
        int reserved = bookCopyRepository.reserveIfAvailable(copyIds);
        if (reserved != copyIds.size()) {
            // Ném lỗi để transaction rollback, các bản đã giữ được trả về AVAILABLE
            throw new BusinessException("Một số cuốn sách vừa được khách khác đặt. Vui lòng kiểm tra lại giỏ hàng và thử lại.");
        }

        User customer = userRepository.getReferenceById(userId);
        Map<Long, List<CartItem>> itemsByStore = new LinkedHashMap<>();
        for (CartItem item : purchasable) {
            itemsByStore.computeIfAbsent(item.getCopy().getStore().getId(), id -> new ArrayList<>()).add(item);
        }

        // Mã giảm giá được kiểm tra và trừ lượt trong cùng transaction với việc giữ chỗ nên lỗi nào cũng hoàn tác tất cả
        PromotionApplicationDTO promotion = null;
        if (!isBlank(request.getPromotionCode())) {
            Map<Long, BigDecimal> subtotalByStore = new LinkedHashMap<>();
            itemsByStore.forEach((storeId, items) -> subtotalByStore.put(storeId, items.stream()
                    .map(item -> item.getCopy().getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add)));
            promotion = promotionService.redeem(request.getPromotionCode(), userId, subtotalByStore);
        }

        List<OrderSummaryDTO> placed = new ArrayList<>();
        for (Map.Entry<Long, List<CartItem>> entry : itemsByStore.entrySet()) {
            BigDecimal discount = promotion == null ? BigDecimal.ZERO
                    : promotion.getDiscountByStoreId().getOrDefault(entry.getKey(), BigDecimal.ZERO);
            Promotion promotionRef = discount.signum() > 0 ? promotionRepository.getReferenceById(promotion.getPromotionId()) : null;
            CustomerOrder order = buildOrder(customer, entry.getValue(), request, delivery, discount, promotionRef,
                    promotionRef == null ? null : promotion.getCode());
            placed.add(orderMapper.toSummary(customerOrderRepository.saveAndFlush(order)));
        }

        cartItemRepository.deleteAll(purchasable);
        return placed;
    }

    private CustomerOrder buildOrder(User customer, List<CartItem> storeItems, CheckoutRequestDTO request, boolean delivery,
                                     BigDecimal discount, Promotion promotion, String promotionCode) {
        Store store = storeItems.get(0).getCopy().getStore();
        BigDecimal subtotal = storeItems.stream()
                .map(item -> item.getCopy().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = delivery ? deliveryFee : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount).add(shippingFee);

        CustomerOrder order = CustomerOrder.builder()
                .orderCode(generateOrderCode())
                .customer(customer)
                .store(store)
                .status(OrderStatus.PENDING)
                .fulfillmentType(request.getFulfillmentType())
                .receiverName(request.getReceiverName().trim())
                .receiverPhone(request.getReceiverPhone().trim())
                .shippingAddress(delivery ? request.getShippingAddress().trim() : null)
                .note(isBlank(request.getNote()) ? null : request.getNote().trim())
                .subtotal(subtotal)
                .discountAmount(discount)
                .promotion(promotion)
                .promotionCode(promotionCode)
                .shippingFee(shippingFee)
                .totalAmount(total)
                .build();

        for (CartItem item : storeItems) {
            BookCopy copy = item.getCopy();
            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .copy(copy)
                    .priceSnapshot(copy.getPrice())
                    .titleSnapshot(copy.getBook().getTitle())
                    .copyCodeSnapshot(copy.getCopyCode())
                    .build());
        }

        order.setPayment(Payment.builder()
                .order(order)
                .method(request.getPaymentMethod())
                .status(PaymentStatus.UNPAID)
                .amount(total)
                .build());
        return order;
    }

    /** Mã đơn dạng OB-yyyyMMdd-XXXXXX, hậu tố ngẫu nhiên để hai khách đặt cùng lúc không đụng mã. */
    private String generateOrderCode() {
        String prefix = "OB-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        String code;
        do {
            StringBuilder suffix = new StringBuilder();
            for (int i = 0; i < CODE_SUFFIX_LENGTH; i++) {
                suffix.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            code = prefix + suffix;
        } while (customerOrderRepository.existsByOrderCode(code));
        return code;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
