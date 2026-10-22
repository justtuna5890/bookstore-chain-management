package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.CheckoutRequestDTO;
import com.bookstorechain.dto.response.OrderLineDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.Cart;
import com.bookstorechain.entity.CartItem;
import com.bookstorechain.entity.CustomerOrder;
import com.bookstorechain.entity.OrderItem;
import com.bookstorechain.entity.Payment;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.CartItemRepository;
import com.bookstorechain.repository.CartRepository;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
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

    @Value("${app.shop.delivery-fee:25000}")
    private BigDecimal deliveryFee;

    @Override
    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    @Override
    public List<OrderSummaryDTO> checkout(Long userId, CheckoutRequestDTO request) {
        // Khoá ngoài transaction để bấm đặt hàng hai lần liên tiếp không tạo trùng đơn
        synchronized (userLocks.lockFor(userId)) {
            return new TransactionTemplate(transactionManager).execute(status -> doCheckout(userId, request));
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

        List<OrderSummaryDTO> placed = new ArrayList<>();
        for (List<CartItem> storeItems : itemsByStore.values()) {
            CustomerOrder order = buildOrder(customer, storeItems, request, delivery);
            placed.add(toSummary(customerOrderRepository.saveAndFlush(order)));
        }

        cartItemRepository.deleteAll(purchasable);
        return placed;
    }

    private CustomerOrder buildOrder(User customer, List<CartItem> storeItems, CheckoutRequestDTO request, boolean delivery) {
        Store store = storeItems.get(0).getCopy().getStore();
        BigDecimal subtotal = storeItems.stream()
                .map(item -> item.getCopy().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = delivery ? deliveryFee : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(shippingFee);

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
                .discountAmount(BigDecimal.ZERO)
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

    private OrderSummaryDTO toSummary(CustomerOrder order) {
        return OrderSummaryDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getName())
                .customerName(order.getReceiverName())
                .status(order.getStatus())
                .fulfillmentType(order.getFulfillmentType())
                .paymentMethod(order.getPayment().getMethod())
                .paymentStatus(order.getPayment().getStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .lines(order.getItems().stream()
                        .map(line -> OrderLineDTO.builder()
                                .copyId(line.getCopy().getId())
                                .bookId(line.getCopy().getBook().getId())
                                .title(line.getTitleSnapshot())
                                .copyCode(line.getCopyCodeSnapshot())
                                .price(line.getPriceSnapshot())
                                .build())
                        .collect(Collectors.toList()))
                .build();
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
