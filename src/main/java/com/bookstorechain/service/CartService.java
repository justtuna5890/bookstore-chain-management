package com.bookstorechain.service;

import com.bookstorechain.dto.response.CartDTO;

public interface CartService {
    /** Giỏ hàng của khách, gom nhóm theo chi nhánh. Trả về giỏ rỗng nếu khách chưa có giỏ. */
    CartDTO getCart(Long userId);

    /** Thêm một bản sách vào giỏ và trả về tổng số sách trong giỏ sau khi thêm. */
    int addItem(Long userId, Long copyId);

    void removeItem(Long userId, Long copyId);

    void clear(Long userId);

    long countItems(Long userId);
}
