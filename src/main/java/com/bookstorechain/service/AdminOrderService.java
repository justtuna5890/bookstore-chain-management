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

    /** CONFIRMED -> READY_FOR_PICKUP (nhận tại chi nhánh) hoặc READY_FOR_DELIVERY (giao tận nơi, chờ shipper nhận). */
    void markReady(Long orderId, Long scopeStoreId);

    /**
     * READY_FOR_PICKUP -> COMPLETED: các bản sách chuyển sang SOLD. Đơn COD được ghi nhận đã thu tiền;
     * đơn chuyển khoản phải được xác nhận thanh toán trước. Đơn giao tận nơi do shipper chốt, không qua đây.
     */
    void complete(Long orderId, Long scopeStoreId);

    /** DELIVERY_FAILED -> READY_FOR_DELIVERY: cho giao lại, bỏ shipper cũ để bất kỳ shipper nào của chi nhánh cũng nhận được. */
    void redeliver(Long orderId, Long scopeStoreId);

    /** Xác nhận đã nhận tiền chuyển khoản của đơn, lưu mã giao dịch ngân hàng. */
    void confirmPayment(Long orderId, Long scopeStoreId, String transactionCode);

    /** Huỷ đơn chưa hoàn tất, trả bản sách về AVAILABLE; đơn đã thanh toán chuyển sang trạng thái hoàn tiền. */
    void cancel(Long orderId, Long scopeStoreId);
}
