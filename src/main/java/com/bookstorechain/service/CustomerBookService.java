package com.bookstorechain.service;

import com.bookstorechain.dto.response.BookDetailDTO;

public interface CustomerBookService {
    /** Chi tiết đầu sách kèm các bản sao đang bán và sách cùng thể loại. Ném ResourceNotFoundException nếu sách không tồn tại hoặc đã ngừng kinh doanh. */
    BookDetailDTO getBookDetail(Long bookId);
}
