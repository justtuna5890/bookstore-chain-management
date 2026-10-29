package com.bookstorechain.service;

import com.bookstorechain.dto.request.ReviewRequestDTO;
import com.bookstorechain.dto.response.RatingSummaryDTO;
import com.bookstorechain.dto.response.ReviewDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Đánh giá sách của khách hàng. Chỉ khách đã nhận hàng (đơn COMPLETED) chứa đầu sách đó mới đánh giá được. */
public interface ReviewService {

    /** Điểm trung bình, số đánh giá và phân bố sao của một đầu sách (chỉ tính đánh giá đang hiển thị). */
    RatingSummaryDTO getSummary(Long bookId);

    /** Điểm trung bình và số đánh giá cho nhiều đầu sách trong một truy vấn; sách chưa có đánh giá có count = 0. */
    Map<Long, RatingSummaryDTO> getSummaries(Collection<Long> bookIds);

    /** Các đánh giá đang hiển thị, mới nhất trước. */
    List<ReviewDTO> getReviews(Long bookId, int limit);

    Optional<ReviewDTO> getMyReview(Long userId, Long bookId);

    /** Khách đã nhận một đơn có chứa đầu sách này chưa. */
    boolean canReview(Long userId, Long bookId);

    /** Gửi hoặc sửa đánh giá của khách. Ném BusinessException nếu chưa mua và nhận sách. */
    ReviewDTO submit(Long userId, Long bookId, ReviewRequestDTO request);

    /** Xoá đánh giá của chính khách; không làm gì nếu chưa có. */
    void deleteOwn(Long userId, Long bookId);
}
