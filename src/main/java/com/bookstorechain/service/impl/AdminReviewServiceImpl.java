package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ReviewDTO;
import com.bookstorechain.entity.Review;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.ReviewRepository;
import com.bookstorechain.service.AdminReviewService;
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
        review.setHidden(hidden);
        return toDTO(reviewRepository.save(review));
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
