package com.bookstorechain.service;

import com.bookstorechain.dto.response.OrderSummaryDTO;

public interface OrderNotificationService {
    /**
     * Báo realtime cho nhân viên chi nhánh nhận đơn và quản trị viên rằng có đơn mới.
     * Gọi sau khi transaction đặt hàng đã commit; không bao giờ ném lỗi để sự cố WebSocket không làm hỏng đơn đã tạo.
     */
    void notifyNewOrder(OrderSummaryDTO order);
}
