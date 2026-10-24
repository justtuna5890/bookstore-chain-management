package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ReviewDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.Review;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.service.UserNotificationService;
import com.bookstorechain.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceImplTest {

    private static final long REVIEW_ID = 9L;

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserNotificationService userNotificationService;

    private AdminReviewServiceImpl service;
    private Review review;

    @BeforeEach
    void setUp() {
        service = new AdminReviewServiceImpl(reviewRepository, userNotificationService);
        User customer = TestData.customer(7L);
        Book book = TestData.book(100L, "Hoang Tu Be");
        review = Review.builder().user(customer).book(book).rating(1).comment("spam").build();
        review.setId(REVIEW_ID);
    }

    @Test
    @DisplayName("Ẩn đánh giá: đặt cờ ẩn và lưu")
    void hide() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDTO result = service.setHidden(REVIEW_ID, true);

        assertThat(review.isHidden()).isTrue();
        assertThat(result.isHidden()).isTrue();
        assertThat(result.getBookTitle()).isEqualTo("Hoang Tu Be");
        verify(reviewRepository).save(review);
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.REVIEW),
                eq("Đánh giá của bạn đã bị ẩn"), contains("Hoang Tu Be"), eq("/books/100#reviews"));
    }

    @Test
    @DisplayName("Ẩn đánh giá đã ẩn sẵn thì không báo lại cho người viết")
    void hidingTwiceDoesNotNotifyAgain() {
        review.setHidden(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        service.setHidden(REVIEW_ID, true);

        verifyNoInteractions(userNotificationService);
    }

    @Test
    @DisplayName("Hiện lại đánh giá đã ẩn")
    void show() {
        review.setHidden(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.setHidden(REVIEW_ID, false).isHidden()).isFalse();
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.REVIEW),
                eq("Đánh giá của bạn đã hiển thị lại"), contains("Hoang Tu Be"), eq("/books/100#reviews"));
    }

    @Test
    @DisplayName("Đánh giá không tồn tại báo không tìm thấy và không lưu gì")
    void unknownReview() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setHidden(REVIEW_ID, true)).isInstanceOf(ResourceNotFoundException.class);

        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(userNotificationService);
    }

    @Test
    @DisplayName("Từ khoá được viết thường, bọc %...% và thoát ký tự đại diện của LIKE")
    void searchBuildsSafePattern() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
        when(reviewRepository.search(any(), any(), any(), eq(pageable))).thenReturn(page);
        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);

        Page<ReviewDTO> result = service.search("  100%_Sale\\  ", true, 1, pageable);

        verify(reviewRepository).search(pattern.capture(), eq(true), eq(1), eq(pageable));
        assertThat(pattern.getValue()).isEqualTo("%100\\%\\_sale\\\\%");
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Không có từ khoá thì khớp mọi dòng")
    void blankKeywordMatchesAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(reviewRepository.search(any(), any(), any(), eq(pageable))).thenReturn(Page.empty(pageable));
        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);

        service.search(null, null, null, pageable);
        service.search("   ", null, null, pageable);

        verify(reviewRepository, org.mockito.Mockito.times(2)).search(pattern.capture(), eq(null), eq(null), eq(pageable));
        assertThat(pattern.getAllValues()).containsOnly("%");
    }

    @Test
    @DisplayName("Đếm tổng số đánh giá và số đang bị ẩn")
    void counts() {
        when(reviewRepository.count()).thenReturn(6L);
        when(reviewRepository.countByHidden(true)).thenReturn(1L);

        assertThat(service.countAll()).isEqualTo(6);
        assertThat(service.countHidden()).isEqualTo(1);
    }
}
