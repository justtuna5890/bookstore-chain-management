package com.bookstorechain.service;

import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Nghiệp vụ nhân viên xử lý đơn hàng của khách. Tham số scopeStoreId là chi nhánh mà người xử lý bị giới hạn
 * (nhân viên); null nghĩa là không giới hạn (quản lý, quản trị).
 */
public interface AdminOrderService {

    Page<OrderSummaryDTO> search(Long storeId, OrderStatus status, String keyword, Pageable pageable);

    /** Ném ResourceNotFoundException nếu không có đơn, BusinessException nếu đơn thuộc chi nhánh khác scopeStoreId. */
    OrderSummaryDTO getOrder(Long orderId, Long scopeStoreId);

    /** PENDING -> CONFIRMED. */
    void confirm(Long orderId, Long scopeStoreId);

    /** CONFIRMED -> READY_FOR_PICKUP (nhận tại chi nhánh) hoặc SHIPPING (giao tận nơi). */
    void markReady(Long orderId, Long scopeStoreId);

    /**
     * READY_FOR_PICKUP/SHIPPING -> COMPLETED: các bản sách chuyển sang SOLD. Đơn COD được ghi nhận đã thu tiền;
     * đơn chuyển khoản phải được xác nhận thanh toán trước.
     */
    void complete(Long orderId, Long scopeStoreId);

    /** Xác nhận đã nhận tiền chuyển khoản của đơn, lưu mã giao dịch ngân hàng. */
    void confirmPayment(Long orderId, Long scopeStoreId, String transactionCode);

    /** Huỷ đơn chưa hoàn tất, trả bản sách về AVAILABLE; đơn đã thanh toán chuyển sang trạng thái hoàn tiền. */
    void cancel(Long orderId, Long scopeStoreId);
}
