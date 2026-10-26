package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.ReviewRequestDTO;
import com.bookstorechain.dto.response.RatingSummaryDTO;
import com.bookstorechain.dto.response.ReviewDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.Review;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.OrderItemRepository;
import com.bookstorechain.repository.ReviewRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserLocks userLocks;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryDTO getSummary(Long bookId) {
        Object[] stats = reviewRepository.stats(bookId).get(0);
        int count = ((Number) stats[1]).intValue();
        double average = stats[0] == null ? 0 : ((Number) stats[0]).doubleValue();

        int[] byStar = new int[6];
        for (Object[] row : reviewRepository.distribution(bookId)) {
            byStar[((Number) row[0]).intValue()] = ((Number) row[1]).intValue();
        }
        List<RatingSummaryDTO.StarBar> bars = new ArrayList<>();
        for (int star = 5; star >= 1; star--) {
            bars.add(new RatingSummaryDTO.StarBar(star, byStar[star], count == 0 ? 0 : Math.round(byStar[star] * 100f / count)));
        }
        return RatingSummaryDTO.builder().bookId(bookId).average(average).count(count).bars(bars).build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, RatingSummaryDTO> getSummaries(Collection<Long> bookIds) {
        Map<Long, RatingSummaryDTO> result = new HashMap<>();
        if (bookIds.isEmpty()) {
            return result;
        }
        for (Object[] row : reviewRepository.summaries(bookIds)) {
            Long bookId = ((Number) row[0]).longValue();
            result.put(bookId, RatingSummaryDTO.builder().bookId(bookId)
                    .average(((Number) row[1]).doubleValue()).count(((Number) row[2]).intValue()).build());
        }
        bookIds.forEach(id -> result.computeIfAbsent(id, key -> RatingSummaryDTO.builder().bookId(key).build()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviews(Long bookId, int limit) {
        return reviewRepository.findByBookIdAndHiddenFalse(bookId, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewDTO> getMyReview(Long userId, Long bookId) {
        return reviewRepository.findByUserIdAndBookId(userId, bookId).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReview(Long userId, Long bookId) {
        return orderItemRepository.hasCompletedPurchase(userId, bookId);
    }

    @Override
    public ReviewDTO submit(Long userId, Long bookId, ReviewRequestDTO request) {
        // Khoá theo khách ngoài transaction: bấm gửi hai lần không vi phạm ràng buộc duy nhất (user, book)
        synchronized (userLocks.lockFor(userId)) {
            return new TransactionTemplate(transactionManager).execute(status -> doSubmit(userId, bookId, request));
        }
    }

    private ReviewDTO doSubmit(Long userId, Long bookId, ReviewRequestDTO request) {
        Integer rating = request.getRating();
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("Số sao phải từ 1 đến 5.");
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đầu sách với ID: " + bookId));
        if (!orderItemRepository.hasCompletedPurchase(userId, bookId)) {
            throw new BusinessException("Bạn chỉ đánh giá được những cuốn sách đã mua và nhận hàng.");
        }

        String comment = request.getComment() == null || request.getComment().isBlank() ? null : request.getComment().trim();
        // Sửa đánh giá cũ nếu có; trạng thái ẩn do quản trị đặt được giữ nguyên để không né kiểm duyệt bằng cách sửa lại
        Review review = reviewRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> Review.builder().user(userRepository.getReferenceById(userId)).book(book).build());
        review.setRating(rating);
        review.setComment(comment);
        return toDTO(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteOwn(Long userId, Long bookId) {
        reviewRepository.findByUserIdAndBookId(userId, bookId).ifPresent(reviewRepository::delete);
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
