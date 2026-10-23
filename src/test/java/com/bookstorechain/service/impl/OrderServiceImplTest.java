package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.CheckoutRequestDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.Cart;
import com.bookstorechain.entity.CartItem;
import com.bookstorechain.entity.CustomerOrder;
import com.bookstorechain.entity.OrderItem;
import com.bookstorechain.entity.Payment;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.enums.PaymentStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.CartItemRepository;
import com.bookstorechain.repository.CartRepository;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final long USER_ID = 1L;

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private CustomerOrderRepository customerOrderRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlatformTransactionManager transactionManager;

    private OrderServiceImpl service;
    private User customer;
    private Cart cart;
    private Store hanoi;
    private Store saigon;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(cartRepository, cartItemRepository, bookCopyRepository,
                customerOrderRepository, userRepository, new UserLocks(), transactionManager, new OrderMapper());
        ReflectionTestUtils.setField(service, "deliveryFee", new BigDecimal("25000"));
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        customer = TestData.customer(USER_ID);
        cart = TestData.cart(10L, customer);
        hanoi = TestData.store(1L, "OldBook Ha Noi");
        saigon = TestData.store(2L, "OldBook Sai Gon");
        book = TestData.book(100L, "Hoang Tu Be");
    }

    private CheckoutRequestDTO request(FulfillmentType fulfillment, String address) {
        return CheckoutRequestDTO.builder()
                .receiverName(" Nguyen Van A ")
                .receiverPhone("0901234567")
                .fulfillmentType(fulfillment)
                .shippingAddress(address)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();
    }

    private void stubCart(CartItem... items) {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId())).thenReturn(List.of(items));
    }

    private void stubSave() {
        AtomicLong ids = new AtomicLong(500);
        when(customerOrderRepository.saveAndFlush(any(CustomerOrder.class))).thenAnswer(invocation -> {
            CustomerOrder order = invocation.getArgument(0);
            order.setId(ids.incrementAndGet());
            return order;
        });
    }

    @Test
    @DisplayName("Giỏ có sách ở 2 chi nhánh được tách thành 2 đơn, giữ chỗ theo thứ tự id tăng dần")
    void checkoutSplitsOrdersByStore() {
        BookCopy c1 = TestData.copy(7L, "OB-SG-001", "55000", saigon, book, CopyStatus.AVAILABLE);
        BookCopy c2 = TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.AVAILABLE);
        BookCopy c3 = TestData.copy(2L, "OB-HN-002", "50000", hanoi, book, CopyStatus.AVAILABLE);
        CartItem i1 = TestData.cartItem(cart, c1);
        CartItem i2 = TestData.cartItem(cart, c2);
        CartItem i3 = TestData.cartItem(cart, c3);
        stubCart(i1, i2, i3);
        when(bookCopyRepository.reserveIfAvailable(any())).thenReturn(3);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(customerOrderRepository.existsByOrderCode(any())).thenReturn(false);
        stubSave();

        List<OrderSummaryDTO> placed = service.checkout(USER_ID, request(FulfillmentType.PICKUP, null));

        assertThat(placed).hasSize(2);
        assertThat(placed.get(0).getStoreId()).isEqualTo(2L);
        assertThat(placed.get(0).getSubtotal()).isEqualByComparingTo("55000");
        assertThat(placed.get(1).getStoreId()).isEqualTo(1L);
        assertThat(placed.get(1).getSubtotal()).isEqualByComparingTo("110000");
        assertThat(placed).allSatisfy(order -> {
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
            assertThat(order.getShippingFee()).isEqualByComparingTo("0");
            assertThat(order.getTotalAmount()).isEqualByComparingTo(order.getSubtotal());
            assertThat(order.getOrderCode()).matches("OB-\\d{8}-[A-Z0-9]{6}");
        });

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> reserved = ArgumentCaptor.forClass(Collection.class);
        verify(bookCopyRepository).reserveIfAvailable(reserved.capture());
        assertThat(reserved.getValue()).containsExactly(1L, 2L, 7L);
        verify(cartItemRepository).deleteAll(List.of(i1, i2, i3));
    }

    @Test
    @DisplayName("Giao tận nơi cộng phí giao hàng cho mỗi đơn và lưu địa chỉ đã cắt khoảng trắng")
    void checkoutAppliesDeliveryFeePerOrder() {
        stubCart(TestData.cartItem(cart, TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.AVAILABLE)),
                TestData.cartItem(cart, TestData.copy(7L, "OB-SG-001", "55000", saigon, book, CopyStatus.AVAILABLE)));
        when(bookCopyRepository.reserveIfAvailable(any())).thenReturn(2);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        stubSave();

        List<OrderSummaryDTO> placed = service.checkout(USER_ID, request(FulfillmentType.DELIVERY, "  12 Pho Hue  "));

        assertThat(placed).extracting(OrderSummaryDTO::getTotalAmount)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("85000"), new BigDecimal("80000"));
        assertThat(placed).allSatisfy(order -> {
            assertThat(order.getShippingFee()).isEqualByComparingTo("25000");
            assertThat(order.getShippingAddress()).isEqualTo("12 Pho Hue");
            assertThat(order.getCustomerName()).isEqualTo("Nguyen Van A");
        });
    }

    @Test
    @DisplayName("Một cuốn vừa bị người khác giữ: ném lỗi, rollback, không tạo đơn và không xoá giỏ")
    void checkoutRollsBackWhenAnotherCustomerTookACopy() {
        stubCart(TestData.cartItem(cart, TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.AVAILABLE)),
                TestData.cartItem(cart, TestData.copy(2L, "OB-HN-002", "50000", hanoi, book, CopyStatus.AVAILABLE)));
        // Entity còn AVAILABLE do đọc từ trước, nhưng UPDATE có điều kiện chỉ giữ được 1 trong 2 cuốn
        when(bookCopyRepository.reserveIfAvailable(any())).thenReturn(1);

        assertThatThrownBy(() -> service.checkout(USER_ID, request(FulfillmentType.PICKUP, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vừa được khách khác đặt");

        verify(customerOrderRepository, never()).saveAndFlush(any());
        verify(cartItemRepository, never()).deleteAll(any());
        verify(transactionManager).rollback(any());
        verify(transactionManager, never()).commit(any());
    }

    @Test
    @DisplayName("Sách không còn AVAILABLE bị bỏ qua, không đưa vào đơn")
    void checkoutSkipsUnavailableCopies() {
        stubCart(TestData.cartItem(cart, TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.SOLD)),
                TestData.cartItem(cart, TestData.copy(2L, "OB-HN-002", "50000", hanoi, book, CopyStatus.AVAILABLE)));
        when(bookCopyRepository.reserveIfAvailable(any())).thenReturn(1);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        stubSave();

        List<OrderSummaryDTO> placed = service.checkout(USER_ID, request(FulfillmentType.PICKUP, null));

        assertThat(placed).hasSize(1);
        assertThat(placed.get(0).getLines()).extracting("copyCode").containsExactly("OB-HN-002");
    }

    @Test
    @DisplayName("Toàn bộ sách trong giỏ đã hết khả dụng thì từ chối đặt hàng")
    void checkoutRejectsWhenNothingIsAvailable() {
        stubCart(TestData.cartItem(cart, TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.RESERVED)));

        assertThatThrownBy(() -> service.checkout(USER_ID, request(FulfillmentType.PICKUP, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không có cuốn sách nào còn khả dụng");

        verify(bookCopyRepository, never()).reserveIfAvailable(any());
    }

    @Test
    @DisplayName("Giao tận nơi mà thiếu địa chỉ thì từ chối trước khi đụng tới giỏ hàng")
    void checkoutRequiresAddressForDelivery() {
        assertThatThrownBy(() -> service.checkout(USER_ID, request(FulfillmentType.DELIVERY, "   ")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("địa chỉ giao hàng");

        verify(cartRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("Giỏ hàng chưa tồn tại thì báo giỏ trống")
    void checkoutRejectsMissingCart() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkout(USER_ID, request(FulfillmentType.PICKUP, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("trống");
    }

    private CustomerOrder pendingOrder(PaymentStatus paymentStatus, OrderStatus status, BookCopy... copies) {
        CustomerOrder order = CustomerOrder.builder()
                .orderCode("OB-20261023-ABCDEF").customer(customer).store(hanoi).status(status)
                .fulfillmentType(FulfillmentType.PICKUP).receiverName("A").receiverPhone("0901234567")
                .subtotal(BigDecimal.TEN).totalAmount(BigDecimal.TEN).build();
        for (BookCopy copy : copies) {
            order.getItems().add(OrderItem.builder().order(order).copy(copy).priceSnapshot(copy.getPrice())
                    .titleSnapshot("t").copyCodeSnapshot(copy.getCopyCode()).build());
        }
        order.setPayment(Payment.builder().order(order).method(PaymentMethod.COD).status(paymentStatus)
                .amount(BigDecimal.TEN).build());
        return order;
    }

    @Test
    @DisplayName("Khách huỷ đơn PENDING chưa thanh toán: đơn CANCELLED và sách RESERVED quay về AVAILABLE")
    void cancelReleasesReservedCopies() {
        BookCopy copy = TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.RESERVED);
        CustomerOrder order = pendingOrder(PaymentStatus.UNPAID, OrderStatus.PENDING, copy);
        when(customerOrderRepository.lockByOrderCodeAndCustomerId("OB-20261023-ABCDEF", USER_ID)).thenReturn(Optional.of(order));

        service.cancelByCustomer(USER_ID, "OB-20261023-ABCDEF");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(copy.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Đơn đã xác nhận không thể tự huỷ và không động tới trạng thái sách")
    void cancelRejectsProcessedOrder() {
        BookCopy copy = TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.RESERVED);
        CustomerOrder order = pendingOrder(PaymentStatus.UNPAID, OrderStatus.CONFIRMED, copy);
        when(customerOrderRepository.lockByOrderCodeAndCustomerId("OB-20261023-ABCDEF", USER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelByCustomer(USER_ID, "OB-20261023-ABCDEF"))
                .isInstanceOf(BusinessException.class);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(copy.getStatus()).isEqualTo(CopyStatus.RESERVED);
    }

    @Test
    @DisplayName("Đơn đã thanh toán không thể tự huỷ")
    void cancelRejectsPaidOrder() {
        CustomerOrder order = pendingOrder(PaymentStatus.PAID, OrderStatus.PENDING,
                TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.RESERVED));
        when(customerOrderRepository.lockByOrderCodeAndCustomerId("OB-20261023-ABCDEF", USER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelByCustomer(USER_ID, "OB-20261023-ABCDEF"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Huỷ đơn của người khác hoặc mã không tồn tại cùng trả 'không tìm thấy', không lộ sự tồn tại của đơn")
    void cancelTreatsForeignOrderAsMissing() {
        when(customerOrderRepository.lockByOrderCodeAndCustomerId("OB-20261023-ABCDEF", 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelByCustomer(99L, "OB-20261023-ABCDEF"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
