package com.bookstorechain.service;

import com.bookstorechain.dto.request.CheckoutRequestDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    /**
     * Đặt hàng toàn bộ sách còn khả dụng trong giỏ. Mỗi chi nhánh tạo một đơn riêng.
     * Việc giữ chỗ bản sách là tất cả-hoặc-không: nếu một cuốn vừa bị khách khác lấy, không đơn nào được tạo.
     */
    List<OrderSummaryDTO> checkout(Long userId, CheckoutRequestDTO request);

    /** Phí giao hàng áp dụng cho mỗi đơn giao tận nơi. */
    BigDecimal getDeliveryFee();

    /** Các đơn của khách, mới nhất trước. */
    List<OrderSummaryDTO> getCustomerOrders(Long userId);

    /** Chi tiết một đơn của khách. Ném ResourceNotFoundException nếu mã không tồn tại hoặc đơn của người khác. */
    OrderSummaryDTO getCustomerOrder(Long userId, String orderCode);

    /** Khách tự huỷ đơn PENDING chưa thanh toán, trả các bản sách về AVAILABLE. */
    void cancelByCustomer(Long userId, String orderCode);
}
