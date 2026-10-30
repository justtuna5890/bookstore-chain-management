package com.bookstorechain.service;

import com.bookstorechain.dto.response.OrderSummaryDTO;

import java.util.List;

/**
 * Nghiệp vụ của shipper. Shipper chỉ thấy đơn của chi nhánh mình thuộc về (lấy từ tài khoản, không nhận từ request)
 * và chỉ thao tác trên đơn do chính mình nhận.
 */
public interface ShipperOrderService {

    /** Đơn giao tận nơi của chi nhánh đang chờ shipper nhận (READY_FOR_DELIVERY), cũ nhất trước. */
    List<OrderSummaryDTO> findAvailable(Long shipperId);

    /** Đơn shipper đang giữ (SHIPPING), cũ nhất trước. */
    List<OrderSummaryDTO> findMine(Long shipperId);

    /** Các đơn gần đây shipper đã giao xong hoặc giao thất bại, mới nhất trước. */
    List<OrderSummaryDTO> findHistory(Long shipperId);

    /**
     * Chi tiết một đơn: đơn đang chờ giao của chi nhánh mình hoặc đơn do mình phụ trách.
     * Ném ResourceNotFoundException nếu không có đơn, BusinessException nếu đơn không thuộc phạm vi của shipper.
     */
    OrderSummaryDTO getOrder(Long orderId, Long shipperId);

    /** READY_FOR_DELIVERY -> SHIPPING, gán đơn cho shipper; hai shipper cùng bấm thì chỉ một người nhận được. */
    void claim(Long orderId, Long shipperId);

    /** SHIPPING -> COMPLETED: bản sách thành SOLD, đơn COD được ghi nhận đã thu tiền. */
    void deliver(Long orderId, Long shipperId);

    /** SHIPPING -> DELIVERY_FAILED kèm lý do bắt buộc; đơn về chi nhánh để nhân viên cho giao lại hoặc huỷ. */
    void fail(Long orderId, Long shipperId, String reason);
}
