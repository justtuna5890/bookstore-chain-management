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
}
