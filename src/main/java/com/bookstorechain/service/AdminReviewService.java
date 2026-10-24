package com.bookstorechain.service;

import com.bookstorechain.dto.response.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Kiểm duyệt đánh giá của khách: quản trị ẩn các đánh giá vi phạm và hiện lại khi cần. */
public interface AdminReviewService {

    /** Tìm trong mọi đánh giá, kể cả đã ẩn. keyword rỗng, hidden và rating null là không lọc. */
    Page<ReviewDTO> search(String keyword, Boolean hidden, Integer rating, Pageable pageable);

    long countAll();

    long countHidden();

    /** Ẩn hoặc hiện một đánh giá. Đánh giá ẩn không hiển thị cho khách và không tính vào điểm trung bình. */
    ReviewDTO setHidden(Long reviewId, boolean hidden);
}
