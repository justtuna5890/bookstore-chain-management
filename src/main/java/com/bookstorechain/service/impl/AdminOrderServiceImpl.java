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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final int MAX_TRANSACTION_CODE_LENGTH = 100;

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderMapper orderMapper;

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
    }

    @Override
    @Transactional
    public void markReady(Long orderId, Long scopeStoreId) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        requireStatus(order, "Chỉ chuẩn bị giao nhận được đơn đã xác nhận.", OrderStatus.CONFIRMED);
        order.setStatus(order.getFulfillmentType() == FulfillmentType.DELIVERY
                ? OrderStatus.SHIPPING : OrderStatus.READY_FOR_PICKUP);
    }

    @Override
    @Transactional
    public void complete(Long orderId, Long scopeStoreId) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        requireStatus(order, "Chỉ hoàn tất được đơn đang chờ nhận hàng hoặc đang giao.",
                OrderStatus.READY_FOR_PICKUP, OrderStatus.SHIPPING);

        // Kiểm tra hết trước khi sửa bất cứ thứ gì để không để lại đơn bán dở
        for (OrderItem item : order.getItems()) {
            if (item.getCopy().getStatus() != CopyStatus.RESERVED) {
                throw new BusinessException("Bản sách " + item.getCopyCodeSnapshot()
                        + " không còn ở trạng thái giữ cho đơn này. Hãy kiểm tra lại kho trước khi hoàn tất.");
            }
        }
        Payment payment = order.getPayment();
        if (payment.getStatus() == PaymentStatus.UNPAID) {
            if (payment.getMethod() != PaymentMethod.COD) {
                throw new BusinessException("Chưa xác nhận đã nhận tiền chuyển khoản. Hãy xác nhận thanh toán trước khi hoàn tất đơn.");
            }
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
        }
        order.getItems().forEach(item -> item.getCopy().setStatus(CopyStatus.SOLD));
        order.setStatus(OrderStatus.COMPLETED);
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
        payment.setPaidAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void cancel(Long orderId, Long scopeStoreId) {
        CustomerOrder order = lockAndCheck(orderId, scopeStoreId);
        requireStatus(order, "Chỉ huỷ được đơn chưa hoàn tất và chưa bị huỷ.",
                OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.READY_FOR_PICKUP, OrderStatus.SHIPPING);

        order.setStatus(OrderStatus.CANCELLED);
        for (OrderItem item : order.getItems()) {
            BookCopy copy = item.getCopy();
            if (copy.getStatus() == CopyStatus.RESERVED) {
                copy.setStatus(CopyStatus.AVAILABLE);
            }
        }
        // Tiền đã nhận phải được hoàn lại ngoài hệ thống, hệ thống chỉ đánh dấu để nhân viên không bỏ sót
        if (order.getPayment().getStatus() == PaymentStatus.PAID) {
            order.getPayment().setStatus(PaymentStatus.REFUNDED);
        }
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
