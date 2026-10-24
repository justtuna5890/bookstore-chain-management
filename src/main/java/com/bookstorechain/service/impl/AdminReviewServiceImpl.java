package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ReviewDTO;
import com.bookstorechain.entity.Review;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.ReviewRepository;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.service.AdminReviewService;
import com.bookstorechain.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final UserNotificationService userNotificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> search(String keyword, Boolean hidden, Integer rating, Pageable pageable) {
        return reviewRepository.search(toPattern(keyword), hidden, rating, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return reviewRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countHidden() {
        return reviewRepository.countByHidden(true);
    }

    @Override
    @Transactional
    public ReviewDTO setHidden(Long reviewId, boolean hidden) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
        boolean changed = review.isHidden() != hidden;
        review.setHidden(hidden);
        Review saved = reviewRepository.save(review);
        if (changed) {
            // Người viết biết vì sao đánh giá của họ biến mất (hoặc hiện lại) thay vì tưởng hệ thống lỗi
            String bookTitle = review.getBook().getTitle();
            userNotificationService.notify(review.getUser().getId(), UserNotificationType.REVIEW,
                    hidden ? "Đánh giá của bạn đã bị ẩn" : "Đánh giá của bạn đã hiển thị lại",
                    hidden ? "Đánh giá của bạn về \"" + bookTitle + "\" bị ẩn vì không phù hợp với quy định cộng đồng."
                            : "Đánh giá của bạn về \"" + bookTitle + "\" đã được hiển thị trở lại.",
                    "/books/" + review.getBook().getId() + "#reviews");
        }
        return toDTO(saved);
    }

    /** %từ khoá% viết thường; ký tự đại diện của LIKE trong từ khoá được thoát để tìm đúng nghĩa đen. */
    private String toPattern(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "%";
        }
        String escaped = keyword.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private ReviewDTO toDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .bookId(review.getBook().getId())
                .bookTitle(review.getBook().getTitle())
                .userId(review.getUser().getId())
                .reviewerName(review.getUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .hidden(review.isHidden())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
