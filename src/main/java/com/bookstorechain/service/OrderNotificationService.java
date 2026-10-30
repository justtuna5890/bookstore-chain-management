package com.bookstorechain.service;

import com.bookstorechain.dto.response.OrderSummaryDTO;

/**
 * Báo realtime qua WebSocket. Mọi phương thức không bao giờ ném lỗi để sự cố WebSocket không làm hỏng đơn hàng,
 * và khi đang trong transaction thì chỉ gửi sau khi transaction commit. Nội dung không có tên, số điện thoại hay
 * địa chỉ của khách.
 */
public interface OrderNotificationService {
    /**
     * Báo realtime cho nhân viên chi nhánh nhận đơn và quản trị viên rằng có đơn mới.
     * Gọi sau khi transaction đặt hàng đã commit.
     */
    void notifyNewOrder(OrderSummaryDTO order);

    /** Báo cho mọi shipper của chi nhánh biết có đơn mới chờ nhận (đóng gói xong hoặc được cho giao lại). */
    void notifyDeliveryAvailable(OrderSummaryDTO order);

    /** Báo cho các shipper của chi nhánh rằng đơn đã có người nhận để họ làm mới danh sách (không kèm âm báo). */
    void notifyDeliveryTaken(OrderSummaryDTO order);

    /** Báo cho nhân viên chi nhánh và quản trị viên một diễn biến giao hàng cần biết (shipper nhận đơn, giao thất bại). */
    void notifyStaffDelivery(OrderSummaryDTO order, String title, String message);
}
