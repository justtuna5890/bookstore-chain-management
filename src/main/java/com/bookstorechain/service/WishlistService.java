package com.bookstorechain.service;

import com.bookstorechain.dto.response.BookCatalogItemDTO;

import java.util.List;
import java.util.Set;

/** Danh sách sách yêu thích của khách hàng. */
public interface WishlistService {

    /** Thêm vào yêu thích nếu chưa có, bỏ đi nếu đã có. Trả về true nếu sau thao tác sách đang được yêu thích. */
    boolean toggle(Long userId, Long bookId);

    /** Id các đầu sách khách đang yêu thích, dùng để tô sẵn biểu tượng trái tim. */
    Set<Long> getBookIds(Long userId);

    /** Các đầu sách yêu thích kèm giá thấp nhất, số bản đang bán và điểm đánh giá; thêm gần đây nhất trước. */
    List<BookCatalogItemDTO> getItems(Long userId);

    /**
     * Báo cho khách có sách yêu thích vừa có hàng lại (mỗi lần có hàng báo một lần). Gọi định kỳ nên bắt được mọi nguồn
     * nhập kho: thêm bản sách, thu mua hoàn tất, nhận chuyển kho, huỷ đơn trả sách. Trả về số thông báo đã tạo.
     */
    int notifyBackInStock();
}
