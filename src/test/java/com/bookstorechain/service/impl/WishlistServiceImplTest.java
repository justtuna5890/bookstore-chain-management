package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.BookCatalogItemDTO;
import com.bookstorechain.dto.response.RatingSummaryDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.entity.WishlistItem;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.repository.WishlistItemRepository;
import com.bookstorechain.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    private static final long USER_ID = 7L;
    private static final long BOOK_ID = 100L;

    @Mock private WishlistItemRepository wishlistItemRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReviewService reviewService;
    @Mock private PlatformTransactionManager transactionManager;

    private WishlistServiceImpl service;
    private User customer;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new WishlistServiceImpl(wishlistItemRepository, bookRepository, bookCopyRepository, userRepository,
                reviewService, new UserLocks(), transactionManager);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        customer = TestData.customer(USER_ID);
        book = TestData.book(BOOK_ID, "Hoang Tu Be");
    }

    @Test
    @DisplayName("Bấm tim lần đầu: thêm sách vào yêu thích")
    void toggleAddsWhenAbsent() {
        when(wishlistItemRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.empty());
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);

        assertThat(service.toggle(USER_ID, BOOK_ID)).isTrue();

        ArgumentCaptor<WishlistItem> saved = ArgumentCaptor.forClass(WishlistItem.class);
        verify(wishlistItemRepository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(customer);
        assertThat(saved.getValue().getBook()).isSameAs(book);
    }

    @Test
    @DisplayName("Bấm tim khi đã yêu thích: bỏ khỏi danh sách, không tạo dòng mới")
    void toggleRemovesWhenPresent() {
        WishlistItem existing = WishlistItem.builder().user(customer).book(book).build();
        when(wishlistItemRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.of(existing));

        assertThat(service.toggle(USER_ID, BOOK_ID)).isFalse();

        verify(wishlistItemRepository).delete(existing);
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đầu sách không tồn tại hoặc đã ngừng bán thì không thêm được")
    void toggleRejectsMissingOrInactiveBook() {
        when(wishlistItemRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.empty());
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.toggle(USER_ID, BOOK_ID)).isInstanceOf(ResourceNotFoundException.class);

        book.setActive(false);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        assertThatThrownBy(() -> service.toggle(USER_ID, BOOK_ID)).isInstanceOf(ResourceNotFoundException.class);

        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sách đã ngừng bán nhưng còn trong yêu thích vẫn bỏ tim được")
    void toggleCanRemoveInactiveBook() {
        book.setActive(false);
        WishlistItem existing = WishlistItem.builder().user(customer).book(book).build();
        when(wishlistItemRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.of(existing));

        assertThat(service.toggle(USER_ID, BOOK_ID)).isFalse();

        verify(wishlistItemRepository).delete(existing);
    }

    @Test
    @DisplayName("getBookIds trả về tập id sách yêu thích")
    void bookIds() {
        when(wishlistItemRepository.findBookIdsByUserId(USER_ID)).thenReturn(List.of(1L, 4L, 4L));

        assertThat(service.getBookIds(USER_ID)).containsExactlyInAnyOrder(1L, 4L);
    }

    @Test
    @DisplayName("Danh sách yêu thích: giá thấp nhất, số bản đang bán, điểm đánh giá; bỏ sách đã ngừng bán")
    void itemsCarryPriceStockAndRating() {
        Store store = TestData.store(1, "HN");
        Book other = TestData.book(200L, "Sach het hang");
        other.setCoverPrice(new BigDecimal("90000"));
        Book retired = TestData.book(300L, "Ngung ban");
        retired.setActive(false);
        when(wishlistItemRepository.findByUserIdWithBook(USER_ID)).thenReturn(List.of(
                WishlistItem.builder().user(customer).book(book).build(),
                WishlistItem.builder().user(customer).book(other).build(),
                WishlistItem.builder().user(customer).book(retired).build()));
        RatingSummaryDTO rating = RatingSummaryDTO.builder().bookId(BOOK_ID).average(4.5).count(2).build();
        when(reviewService.getSummaries(List.of(BOOK_ID, 200L))).thenReturn(Map.of(BOOK_ID, rating));
        when(bookCopyRepository.findByBookIdAndStatus(BOOK_ID, CopyStatus.AVAILABLE)).thenReturn(List.of(
                TestData.copy(1, "C1", "120000", store, book, CopyStatus.AVAILABLE),
                TestData.copy(2, "C2", "80000", store, book, CopyStatus.AVAILABLE)));
        when(bookCopyRepository.findByBookIdAndStatus(200L, CopyStatus.AVAILABLE)).thenReturn(List.of());

        List<BookCatalogItemDTO> items = service.getItems(USER_ID);

        assertThat(items).extracting(BookCatalogItemDTO::getId).containsExactly(BOOK_ID, 200L);
        assertThat(items.get(0).getMinPrice()).isEqualByComparingTo("80000");
        assertThat(items.get(0).getAvailableCopiesCount()).isEqualTo(2);
        assertThat(items.get(0).getRating()).isSameAs(rating);
        assertThat(items.get(1).getAvailableCopiesCount()).isZero();
        assertThat(items.get(1).getMinPrice()).isEqualByComparingTo("90000");
    }

    @Test
    @DisplayName("Chưa yêu thích gì thì danh sách rỗng")
    void emptyWishlist() {
        when(wishlistItemRepository.findByUserIdWithBook(USER_ID)).thenReturn(List.of());
        when(reviewService.getSummaries(List.of())).thenReturn(Map.of());

        assertThat(service.getItems(USER_ID)).isEmpty();
        assertThat(service.getBookIds(USER_ID)).isEqualTo(Set.of());
    }
}
