package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.CustomerOrder;
import com.bookstorechain.entity.OrderItem;
import com.bookstorechain.entity.Payment;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.enums.PaymentStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.service.AdminOrderService;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.service.OrderNotificationService;
import com.bookstorechain.service.PromotionService;
import com.bookstorechain.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final int MAX_TRANSACTION_CODE_LENGTH = 100;

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderMapper orderMapper;
    private final PromotionService promotionService;
    private final UserNotificationService userNotificationService;
    private final OrderCompletionSupport orderCompletionSupport;
    private final OrderNotificationService orderNotificationService;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryDTO> search(Long storeId, OrderStatus status, String keyword, Pageable pageable) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return customerOrderRepository.search(storeId, status, normalized, pageable).map(orderMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryDTO getOrder(Long orderId, Long scopeStoreId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        checkScope(order, scopeStoreId);
        return orderMapper.toSummary(order);
    }

    @Override
    @Transactional
    public void confirm(Long orderId, Long scopeStoreId) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        requireStatus(order, "Chỉ xác nhận được đơn đang chờ xác nhận.", OrderStatus.PENDING);
        order.setStatus(OrderStatus.CONFIRMED);
        notifyCustomer(order, "Đơn " + order.getOrderCode() + " đã được xác nhận",
                order.getStore().getName() + " đang chuẩn bị sách cho bạn.");
    }

    @Override
    @Transactional
    public void markReady(Long orderId, Long scopeStoreId) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        requireStatus(order, "Chỉ chuẩn bị giao nhận được đơn đã xác nhận.", OrderStatus.CONFIRMED);
        boolean delivery = order.getFulfillmentType() == FulfillmentType.DELIVERY;
        order.setStatus(delivery ? OrderStatus.READY_FOR_DELIVERY : OrderStatus.READY_FOR_PICKUP);
        notifyCustomer(order,
                delivery ? "Đơn " + order.getOrderCode() + " đã đóng gói xong" : "Đơn " + order.getOrderCode() + " sẵn sàng để nhận",
                delivery ? "Sách của bạn đã sẵn sàng, đang chờ shipper nhận giao."
                        : "Mời bạn đến " + order.getStore().getName() + " để nhận sách.");
        if (delivery) {
            orderNotificationService.notifyDeliveryAvailable(orderMapper.toSummary(order));
        }
    }

    @Override
    @Transactional
    public void complete(Long orderId, Long scopeStoreId) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        requireStatus(order, "Chỉ hoàn tất được đơn nhận tại cửa hàng đang chờ khách đến nhận. Đơn giao tận nơi do shipper xác nhận đã giao.",
                OrderStatus.READY_FOR_PICKUP);
        orderCompletionSupport.complete(order);
    }

    @Override
    @Transactional
    public void redeliver(Long orderId, Long scopeStoreId) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        requireStatus(order, "Chỉ cho giao lại được đơn giao thất bại.", OrderStatus.DELIVERY_FAILED);
        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        order.setShipper(null);
        order.setDeliveryFailureReason(null);
        notifyCustomer(order, "Đơn " + order.getOrderCode() + " sẽ được giao lại",
                "Chi nhánh đã sắp xếp giao lại đơn của bạn, đang chờ shipper nhận.");
        orderNotificationService.notifyDeliveryAvailable(orderMapper.toSummary(order));
    }

    @Override
    @Transactional
    public void confirmPayment(Long orderId, Long scopeStoreId, String transactionCode) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        Payment payment = order.getPayment();

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Không thể xác nhận thanh toán cho đơn đã hoàn tất hoặc đã huỷ.");
        }
        if (payment.getMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new BusinessException("Đơn thanh toán khi nhận hàng được ghi nhận tự động lúc hoàn tất đơn.");
        }
        if (payment.getStatus() != PaymentStatus.UNPAID) {
            throw new BusinessException("Đơn này đã được xác nhận thanh toán.");
        }
        String code = transactionCode == null ? "" : transactionCode.trim();
        if (code.isEmpty()) {
            throw new BusinessException("Vui lòng nhập mã giao dịch ngân hàng.");
        }
        if (code.length() > MAX_TRANSACTION_CODE_LENGTH) {
            throw new BusinessException("Mã giao dịch tối đa " + MAX_TRANSACTION_CODE_LENGTH + " ký tự.");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionCode(code);
        payment.setPaidAt(LocalDateTime.now(clock));
        notifyCustomer(order, "Đã nhận thanh toán đơn " + order.getOrderCode(),
                "Chi nhánh đã xác nhận khoản chuyển khoản của bạn.");
    }

    @Override
    @Transactional
    public void cancel(Long orderId, Long scopeStoreId) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        requireStatus(order, "Chỉ huỷ được đơn chưa hoàn tất và chưa bị huỷ.",
                OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.READY_FOR_PICKUP, OrderStatus.READY_FOR_DELIVERY,
                OrderStatus.SHIPPING, OrderStatus.DELIVERY_FAILED);

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
        // Tiền đã nhận phải được hoàn lại ngoài hệ thống, hệ thống chỉ đánh dấu để nhân viên không bỏ sót
        boolean refund = order.getPayment().getStatus() == PaymentStatus.PAID;
        if (refund) {
            order.getPayment().setStatus(PaymentStatus.REFUNDED);
        }
        notifyCustomer(order, "Đơn " + order.getOrderCode() + " đã bị huỷ",
                "Chi nhánh đã huỷ đơn của bạn." + (refund ? " Chi nhánh sẽ hoàn lại tiền bạn đã thanh toán." : ""));
    }

    /** Thông báo cho khách đặt đơn; lưu cùng transaction nên đổi trạng thái và thông báo cùng thành công hoặc cùng huỷ. */
    private void notifyCustomer(CustomerOrder order, String title, String message) {
        userNotificationService.notify(order.getCustomer().getId(), UserNotificationType.ORDER, title, message,
                "/orders/" + order.getOrderCode());
    }

    private CustomerOrder lockAndCheck(Long orderId, Long scopeStoreId) {
        CustomerOrder order = customerOrderRepository.lockById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        checkScope(order, scopeStoreId);
        return order;
    }

    private void checkScope(CustomerOrder order, Long scopeStoreId) {
        if (scopeStoreId != null && !scopeStoreId.equals(order.getStore().getId())) {
            throw new BusinessException("Bạn chỉ có quyền xử lý đơn hàng của chi nhánh mình phụ trách.");
        }
    }

    private void requireStatus(CustomerOrder order, String message, OrderStatus... allowed) {
        for (OrderStatus status : allowed) {
            if (order.getStatus() == status) {
                return;
            }
        }
        throw new BusinessException(message);
    }
}
