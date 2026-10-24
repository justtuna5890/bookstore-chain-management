package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.CartDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.Cart;
import com.bookstorechain.entity.CartItem;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.CartItemRepository;
import com.bookstorechain.repository.CartRepository;
import com.bookstorechain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
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
class CartServiceImplTest {

    private static final long USER_ID = 1L;

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlatformTransactionManager transactionManager;

    private CartServiceImpl service;
    private User customer;
    private Cart cart;
    private Store hanoi;
    private Store saigon;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(cartRepository, cartItemRepository, bookCopyRepository, userRepository,
                new UserLocks(), transactionManager);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        customer = TestData.customer(USER_ID);
        cart = TestData.cart(10L, customer);
        hanoi = TestData.store(1L, "OldBook Ha Noi");
        saigon = TestData.store(2L, "OldBook Sai Gon");
        book = TestData.book(100L, "Hoang Tu Be");
    }

    @Test
    @DisplayName("Thêm sách khả dụng vào giỏ trả về số sách hiện có")
    void addItemReturnsNewCount() {
        BookCopy copy = TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.AVAILABLE);
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(copy));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.existsByCartIdAndCopyId(10L, 1L)).thenReturn(false);
        when(cartItemRepository.countByCartId(10L)).thenReturn(3L);

        assertThat(service.addItem(USER_ID, 1L)).isEqualTo(3);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Khách chưa có giỏ thì giỏ được tạo khi thêm sách đầu tiên")
    void addItemCreatesCartOnFirstUse() {
        BookCopy copy = TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.AVAILABLE);
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(copy));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.existsByCartIdAndCopyId(10L, 1L)).thenReturn(false);
        when(cartItemRepository.countByCartId(10L)).thenReturn(1L);

        assertThat(service.addItem(USER_ID, 1L)).isEqualTo(1);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Bản sách đã bị giữ hoặc đã bán không thêm được vào giỏ")
    void addItemRejectsUnavailableCopy() {
        when(bookCopyRepository.findById(1L))
                .thenReturn(Optional.of(TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.RESERVED)));

        assertThatThrownBy(() -> service.addItem(USER_ID, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không còn sẵn sàng");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thêm trùng một bản sách bị từ chối")
    void addItemRejectsDuplicate() {
        when(bookCopyRepository.findById(1L))
                .thenReturn(Optional.of(TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.AVAILABLE)));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.existsByCartIdAndCopyId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.addItem(USER_ID, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã có trong giỏ");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bản sách không tồn tại báo không tìm thấy")
    void addItemRejectsUnknownCopy() {
        when(bookCopyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addItem(USER_ID, 99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Giỏ hàng gom theo chi nhánh và chỉ cộng tiền các sách còn khả dụng")
    void getCartGroupsByStoreAndExcludesUnavailableFromSubtotal() {
        BookCopy a = TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.AVAILABLE);
        BookCopy b = TestData.copy(7L, "OB-SG-001", "55000", saigon, book, CopyStatus.AVAILABLE);
        BookCopy c = TestData.copy(2L, "OB-HN-002", "50000", hanoi, book, CopyStatus.SOLD);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(
                TestData.cartItem(cart, a), TestData.cartItem(cart, b), TestData.cartItem(cart, c)));

        CartDTO result = service.getCart(USER_ID);

        assertThat(result.getItemCount()).isEqualTo(3);
        assertThat(result.getAvailableCount()).isEqualTo(2);
        assertThat(result.isHasUnavailable()).isTrue();
        assertThat(result.getSubtotal()).isEqualByComparingTo("115000");
        assertThat(result.getGroups()).hasSize(2);
        assertThat(result.getGroups().get(0).getStoreId()).isEqualTo(1L);
        assertThat(result.getGroups().get(0).getItems()).hasSize(2);
        assertThat(result.getGroups().get(0).getSubtotal()).isEqualByComparingTo("60000");
        assertThat(result.getOrderCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Khách chưa có giỏ nhận về giỏ rỗng")
    void getCartReturnsEmptyCartWhenMissing() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        CartDTO result = service.getCart(USER_ID);

        assertThat(result.isEmptyCart()).isTrue();
        assertThat(result.getGroups()).isEmpty();
    }
}
