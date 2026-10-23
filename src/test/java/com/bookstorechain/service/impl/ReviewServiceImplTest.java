package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.ReviewRequestDTO;
import com.bookstorechain.dto.response.RatingSummaryDTO;
import com.bookstorechain.dto.response.ReviewDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.Review;
import com.bookstorechain.entity.User;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.OrderItemRepository;
import com.bookstorechain.repository.ReviewRepository;
import com.bookstorechain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    private static final long USER_ID = 7L;
    private static final long BOOK_ID = 100L;

    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlatformTransactionManager transactionManager;

    private ReviewServiceImpl service;
    private User customer;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new ReviewServiceImpl(reviewRepository, orderItemRepository, bookRepository, userRepository,
                new UserLocks(), transactionManager);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        customer = TestData.customer(USER_ID);
        book = TestData.book(BOOK_ID, "Hoang Tu Be");
    }

    private ReviewRequestDTO request(Integer rating, String comment) {
        return ReviewRequestDTO.builder().rating(rating).comment(comment).build();
    }

    private void bookExistsAndWasBought(boolean bought) {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(orderItemRepository.hasCompletedPurchase(USER_ID, BOOK_ID)).thenReturn(bought);
    }

    @Test
    @DisplayName("Khách đã nhận hàng gửi đánh giá mới: lưu số sao và nhận xét đã cắt khoảng trắng")
    void submitCreatesReview() {
        bookExistsAndWasBought(true);
        when(reviewRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDTO result = service.submit(USER_ID, BOOK_ID, request(4, "  Rất tốt  "));

        ArgumentCaptor<Review> saved = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(saved.capture());
        assertThat(saved.getValue().getRating()).isEqualTo(4);
        assertThat(saved.getValue().getComment()).isEqualTo("Rất tốt");
        assertThat(saved.getValue().isHidden()).isFalse();
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getReviewerName()).isEqualTo("Khach 7");
    }

    @Test
    @DisplayName("Nhận xét để trống được lưu là null")
    void blankCommentBecomesNull() {
        bookExistsAndWasBought(true);
        when(reviewRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.submit(USER_ID, BOOK_ID, request(5, "   ")).getComment()).isNull();
    }

    @Test
    @DisplayName("Gửi lại đánh giá là sửa đánh giá cũ, không tạo bản thứ hai và giữ nguyên trạng thái ẩn do quản trị đặt")
    void submitUpdatesExistingReviewAndKeepsHiddenFlag() {
        bookExistsAndWasBought(true);
        Review existing = Review.builder().user(customer).book(book).rating(1).comment("cu").hidden(true).build();
        when(reviewRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        service.submit(USER_ID, BOOK_ID, request(5, "moi"));

        assertThat(existing.getRating()).isEqualTo(5);
        assertThat(existing.getComment()).isEqualTo("moi");
        assertThat(existing.isHidden()).isTrue();
        verify(userRepository, never()).getReferenceById(any());
    }

    @Test
    @DisplayName("Chưa mua và nhận sách thì không đánh giá được")
    void submitRequiresCompletedPurchase() {
        bookExistsAndWasBought(false);

        assertThatThrownBy(() -> service.submit(USER_ID, BOOK_ID, request(5, "hay")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("đã mua và nhận hàng");

        verify(reviewRepository, never()).save(any());
        verify(transactionManager).rollback(any());
    }

    @Test
    @DisplayName("Số sao ngoài khoảng 1-5 bị từ chối kể cả khi gọi thẳng service")
    void submitRejectsOutOfRangeRating() {
        assertThatThrownBy(() -> service.submit(USER_ID, BOOK_ID, request(0, null))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.submit(USER_ID, BOOK_ID, request(6, null))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.submit(USER_ID, BOOK_ID, request(null, null))).isInstanceOf(BusinessException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sách không tồn tại báo không tìm thấy")
    void submitUnknownBook() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submit(USER_ID, BOOK_ID, request(5, null))).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Tóm tắt: điểm trung bình, số đánh giá và thanh phân bố từ 5 sao xuống 1 sao")
    void summaryHasAverageAndBars() {
        when(reviewRepository.stats(BOOK_ID)).thenReturn(Collections.singletonList(new Object[]{4.25, 4L}));
        when(reviewRepository.distribution(BOOK_ID)).thenReturn(List.of(new Object[]{5, 2L}, new Object[]{4, 1L}, new Object[]{3, 1L}));

        RatingSummaryDTO summary = service.getSummary(BOOK_ID);

        assertThat(summary.getCount()).isEqualTo(4);
        assertThat(summary.getAverageLabel()).isEqualTo("4.3");
        assertThat(summary.getStarPercent()).isEqualTo(85);
        assertThat(summary.getBars()).extracting(RatingSummaryDTO.StarBar::getStar).containsExactly(5, 4, 3, 2, 1);
        assertThat(summary.getBars()).extracting(RatingSummaryDTO.StarBar::getCount).containsExactly(2, 1, 1, 0, 0);
        assertThat(summary.getBars()).extracting(RatingSummaryDTO.StarBar::getPercent).containsExactly(50, 25, 25, 0, 0);
    }

    @Test
    @DisplayName("Sách chưa có đánh giá: trung bình 0, nhãn gạch ngang, mọi thanh 0%")
    void summaryOfUnratedBook() {
        when(reviewRepository.stats(BOOK_ID)).thenReturn(Collections.singletonList(new Object[]{null, 0L}));
        when(reviewRepository.distribution(BOOK_ID)).thenReturn(List.of());

        RatingSummaryDTO summary = service.getSummary(BOOK_ID);

        assertThat(summary.isRated()).isFalse();
        assertThat(summary.getAverageLabel()).isEqualTo("—");
        assertThat(summary.getBars()).extracting(RatingSummaryDTO.StarBar::getPercent).containsOnly(0);
    }

    @Test
    @DisplayName("Tóm tắt nhiều sách: sách không có đánh giá vẫn có mục với count 0")
    void summariesCoverEveryRequestedBook() {
        when(reviewRepository.summaries(List.of(1L, 2L))).thenReturn(List.<Object[]>of(new Object[]{1L, 4.5, 2L}));

        Map<Long, RatingSummaryDTO> result = service.getSummaries(List.of(1L, 2L));

        assertThat(result.get(1L).getAverageLabel()).isEqualTo("4.5");
        assertThat(result.get(1L).getCount()).isEqualTo(2);
        assertThat(result.get(2L).isRated()).isFalse();
    }

    @Test
    @DisplayName("Danh sách sách rỗng thì không truy vấn")
    void summariesOfNothing() {
        assertThat(service.getSummaries(List.of())).isEmpty();

        verify(reviewRepository, never()).summaries(any());
    }

    @Test
    @DisplayName("Danh sách đánh giá hiển thị lấy mới nhất trước với giới hạn số dòng")
    void reviewsAreNewestFirstAndLimited() {
        Review review = Review.builder().user(customer).book(book).rating(5).comment("hay").build();
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(reviewRepository.findByBookIdAndHiddenFalse(any(), any())).thenReturn(List.of(review));

        List<ReviewDTO> result = service.getReviews(BOOK_ID, 20);

        verify(reviewRepository).findByBookIdAndHiddenFalse(any(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Xoá đánh giá của chính mình; chưa có thì không làm gì")
    void deleteOwn() {
        Review review = Review.builder().user(customer).book(book).rating(3).build();
        when(reviewRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.of(review), Optional.empty());

        service.deleteOwn(USER_ID, BOOK_ID);
        service.deleteOwn(USER_ID, BOOK_ID);

        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("canReview theo lịch sử mua của khách")
    void canReviewFollowsPurchaseHistory() {
        when(orderItemRepository.hasCompletedPurchase(USER_ID, BOOK_ID)).thenReturn(true, false);

        assertThat(service.canReview(USER_ID, BOOK_ID)).isTrue();
        assertThat(service.canReview(USER_ID, BOOK_ID)).isFalse();
    }
}
