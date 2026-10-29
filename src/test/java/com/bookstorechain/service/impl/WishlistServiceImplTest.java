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
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.service.ReviewService;
import com.bookstorechain.service.UserNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @Mock private UserNotificationService userNotificationService;
    @Mock private PlatformTransactionManager transactionManager;

    private WishlistServiceImpl service;
    private User customer;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new WishlistServiceImpl(wishlistItemRepository, bookRepository, bookCopyRepository, userRepository,
                reviewService, userNotificationService, new UserLocks(), transactionManager);
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
        assertThat(saved.getValue().isInStockNotified()).as("sách hết hàng: sẽ được báo khi có hàng").isFalse();
    }

    @Test
    @DisplayName("Thêm sách đang có hàng: đánh dấu đã báo để không báo 'có hàng lại' vô lý")
    void toggleAddInStockBookIsMarkedAsNotified() {
        Store store = TestData.store(1, "HN");
        when(wishlistItemRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.empty());
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(bookCopyRepository.findByBookIdAndStatus(BOOK_ID, CopyStatus.AVAILABLE))
                .thenReturn(List.of(TestData.copy(1, "C1", "50000", store, book, CopyStatus.AVAILABLE)));

        service.toggle(USER_ID, BOOK_ID);

        ArgumentCaptor<WishlistItem> saved = ArgumentCaptor.forClass(WishlistItem.class);
        verify(wishlistItemRepository).save(saved.capture());
        assertThat(saved.getValue().isInStockNotified()).isTrue();
    }

    @Test
    @DisplayName("Quét có hàng lại: đặt lại cờ trước, báo từng khách có sách vừa có hàng và đánh dấu đã báo")
    void notifyBackInStockNotifiesOwnersAndMarksItems() {
        User other = TestData.customer(8L);
        Book second = TestData.book(200L, "Nha Gia Kim");
        WishlistItem first = WishlistItem.builder().user(customer).book(book).build();
        WishlistItem secondItem = WishlistItem.builder().user(other).book(second).build();
        when(wishlistItemRepository.findRestocked()).thenReturn(List.of(first, secondItem));

        int sent = service.notifyBackInStock();

        assertThat(sent).isEqualTo(2);
        assertThat(first.isInStockNotified()).isTrue();
        assertThat(secondItem.isInStockNotified()).isTrue();
        InOrder inOrder = inOrder(wishlistItemRepository);
        inOrder.verify(wishlistItemRepository).resetOutOfStock();
        inOrder.verify(wishlistItemRepository).findRestocked();
        verify(userNotificationService).notify(eq(USER_ID), eq(UserNotificationType.WISHLIST),
                eq("Sách yêu thích đã có hàng"), contains("Hoang Tu Be"), eq("/books/100"));
        verify(userNotificationService).notify(eq(8L), eq(UserNotificationType.WISHLIST),
                eq("Sách yêu thích đã có hàng"), contains("Nha Gia Kim"), eq("/books/200"));
    }

    @Test
    @DisplayName("Không có sách nào vừa có hàng thì không báo gì")
    void notifyBackInStockWithNothingToDo() {
        when(wishlistItemRepository.findRestocked()).thenReturn(List.of());

        assertThat(service.notifyBackInStock()).isZero();

        verifyNoInteractions(userNotificationService);
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
