package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.CustomerOrder;
import com.bookstorechain.entity.OrderItem;
import com.bookstorechain.entity.Payment;
import com.bookstorechain.entity.Promotion;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.enums.PaymentStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.service.PromotionService;
import com.bookstorechain.service.UserNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceImplTest {

    private static final long ORDER_ID = 50L;
    private static final java.time.Clock CLOCK = java.time.Clock.fixed(java.time.Instant.parse("2026-10-23T10:00:00Z"), java.time.ZoneId.of("UTC"));

    @Mock private CustomerOrderRepository customerOrderRepository;
    @Mock private PromotionService promotionService;
    @Mock private UserNotificationService userNotificationService;

    private AdminOrderServiceImpl service;
    private Store hanoi;
    private Book book;
    private BookCopy copy1;
    private BookCopy copy2;

    @BeforeEach
    void setUp() {
        service = new AdminOrderServiceImpl(customerOrderRepository, new OrderMapper(), promotionService, userNotificationService,
                CLOCK);
        hanoi = TestData.store(1L, "OldBook Ha Noi");
        book = TestData.book(100L, "Hoang Tu Be");
        copy1 = TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.RESERVED);
        copy2 = TestData.copy(2L, "OB-HN-002", "50000", hanoi, book, CopyStatus.RESERVED);
    }

    private CustomerOrder buildOrder(OrderStatus status, FulfillmentType fulfillment, PaymentMethod method, PaymentStatus payment) {
        CustomerOrder order = CustomerOrder.builder()
                .orderCode("OB-20261023-ABCDEF").customer(TestData.customer(7L)).store(hanoi).status(status)
                .fulfillmentType(fulfillment).receiverName("Khach A").receiverPhone("0901234567")
                .subtotal(new BigDecimal("110000")).totalAmount(new BigDecimal("110000")).build();
        order.setId(ORDER_ID);
        for (BookCopy copy : List.of(copy1, copy2)) {
            order.getItems().add(OrderItem.builder().order(order).copy(copy).priceSnapshot(copy.getPrice())
                    .titleSnapshot("Hoang Tu Be").copyCodeSnapshot(copy.getCopyCode()).build());
        }
        order.setPayment(Payment.builder().order(order).method(method).status(payment)
                .amount(new BigDecimal("110000")).build());
        return order;
    }

    /** Dựng đơn và cho phép nhân viên khoá nó để đổi trạng thái. */
    private CustomerOrder order(OrderStatus status, FulfillmentType fulfillment, PaymentMethod method, PaymentStatus payment) {
        CustomerOrder order = buildOrder(status, fulfillment, method, payment);
        when(customerOrderRepository.lockById(ORDER_ID)).thenReturn(Optional.of(order));
        return order;
    }

    @Test
    @DisplayName("Xác nhận đơn PENDING chuyển sang CONFIRMED")
    void confirmMovesPendingToConfirmed() {
        CustomerOrder order = order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);

        service.confirm(ORDER_ID, null);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261023-ABCDEF đã được xác nhận"), contains("OldBook Ha Noi"), eq("/orders/OB-20261023-ABCDEF"));
    }

    @Test
    @DisplayName("Chỉ đơn PENDING mới xác nhận được, và khi từ chối thì không có thông báo nào")
    void confirmRejectsOtherStatuses() {
        order(OrderStatus.CONFIRMED, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);

        assertThatThrownBy(() -> service.confirm(ORDER_ID, null)).isInstanceOf(BusinessException.class);

        verifyNoInteractions(userNotificationService);
    }

    @Test
    @DisplayName("Đơn nhận tại cửa hàng sang READY_FOR_PICKUP, đơn giao tận nơi sang SHIPPING")
    void markReadyDependsOnFulfillment() {
        CustomerOrder pickup = order(OrderStatus.CONFIRMED, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);
        service.markReady(ORDER_ID, null);
        assertThat(pickup.getStatus()).isEqualTo(OrderStatus.READY_FOR_PICKUP);

        CustomerOrder delivery = order(OrderStatus.CONFIRMED, FulfillmentType.DELIVERY, PaymentMethod.COD, PaymentStatus.UNPAID);
        service.markReady(ORDER_ID, null);
        assertThat(delivery.getStatus()).isEqualTo(OrderStatus.SHIPPING);

        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261023-ABCDEF sẵn sàng để nhận"), contains("OldBook Ha Noi"), any());
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261023-ABCDEF đang được giao"), any(), any());
    }

    @Test
    @DisplayName("Hoàn tất đơn COD: sách thành SOLD, ghi nhận đã thu tiền")
    void completeCodOrderSellsCopiesAndCollectsPayment() {
        CustomerOrder order = order(OrderStatus.SHIPPING, FulfillmentType.DELIVERY, PaymentMethod.COD, PaymentStatus.UNPAID);

        service.complete(ORDER_ID, null);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.SOLD);
        assertThat(copy2.getStatus()).isEqualTo(CopyStatus.SOLD);
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getPayment().getPaidAt()).isEqualTo(java.time.LocalDateTime.of(2026, 10, 23, 10, 0));
        assertThat(order.getCompletedAt()).as("doanh thu tính theo ngày hoàn tất").isEqualTo(java.time.LocalDateTime.of(2026, 10, 23, 10, 0));
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261023-ABCDEF đã hoàn tất"), contains("đánh giá"), eq("/orders/OB-20261023-ABCDEF"));
    }

    @Test
    @DisplayName("Đơn chuyển khoản chưa xác nhận tiền thì chưa hoàn tất được và không bán sách")
    void completeBankTransferRequiresPaymentConfirmation() {
        CustomerOrder order = order(OrderStatus.READY_FOR_PICKUP, FulfillmentType.PICKUP, PaymentMethod.BANK_TRANSFER, PaymentStatus.UNPAID);

        assertThatThrownBy(() -> service.complete(ORDER_ID, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("chuyển khoản");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY_FOR_PICKUP);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.RESERVED);
        verifyNoInteractions(userNotificationService);
        assertThat(order.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("Đơn chuyển khoản đã nhận tiền hoàn tất bình thường")
    void completePaidBankTransferOrder() {
        CustomerOrder order = order(OrderStatus.READY_FOR_PICKUP, FulfillmentType.PICKUP, PaymentMethod.BANK_TRANSFER, PaymentStatus.PAID);

        service.complete(ORDER_ID, null);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.SOLD);
    }

    @Test
    @DisplayName("Bản sách không còn RESERVED (bị chỉnh tay) thì từ chối hoàn tất và không đụng tới thanh toán")
    void completeRejectsCopyThatIsNoLongerReserved() {
        CustomerOrder order = order(OrderStatus.SHIPPING, FulfillmentType.DELIVERY, PaymentMethod.COD, PaymentStatus.UNPAID);
        copy2.setStatus(CopyStatus.AVAILABLE);

        assertThatThrownBy(() -> service.complete(ORDER_ID, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("OB-HN-002");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.RESERVED);
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    @DisplayName("Chưa xác nhận hoặc chưa sẵn sàng thì không hoàn tất được")
    void completeRejectsEarlyStatuses() {
        order(OrderStatus.CONFIRMED, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);

        assertThatThrownBy(() -> service.complete(ORDER_ID, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Xác nhận thanh toán chuyển khoản lưu mã giao dịch đã cắt khoảng trắng")
    void confirmPaymentStoresTransactionCode() {
        CustomerOrder order = order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.BANK_TRANSFER, PaymentStatus.UNPAID);

        service.confirmPayment(ORDER_ID, null, "  FT26102300123  ");

        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getPayment().getTransactionCode()).isEqualTo("FT26102300123");
        assertThat(order.getPayment().getPaidAt()).isNotNull();
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đã nhận thanh toán đơn OB-20261023-ABCDEF"), any(), any());
    }

    @Test
    @DisplayName("Xác nhận thanh toán bị từ chối khi thiếu mã, đơn COD, đã thanh toán hoặc đơn đã đóng")
    void confirmPaymentGuards() {
        order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.BANK_TRANSFER, PaymentStatus.UNPAID);
        assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, null, "   ")).hasMessageContaining("mã giao dịch");

        order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);
        assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, null, "X1")).hasMessageContaining("khi nhận hàng");

        order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.BANK_TRANSFER, PaymentStatus.PAID);
        assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, null, "X1")).hasMessageContaining("đã được xác nhận");

        order(OrderStatus.CANCELLED, FulfillmentType.PICKUP, PaymentMethod.BANK_TRANSFER, PaymentStatus.UNPAID);
        assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, null, "X1")).hasMessageContaining("đã hoàn tất hoặc đã huỷ");

        order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.BANK_TRANSFER, PaymentStatus.UNPAID);
        assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, null, "x".repeat(101))).hasMessageContaining("tối đa");
    }

    @Test
    @DisplayName("Nhân viên huỷ đơn: sách về AVAILABLE, đơn chưa thanh toán giữ UNPAID")
    void cancelReleasesCopies() {
        CustomerOrder order = order(OrderStatus.CONFIRMED, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);

        service.cancel(ORDER_ID, null);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
        assertThat(copy2.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.UNPAID);
        verifyNoInteractions(promotionService);
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261023-ABCDEF đã bị huỷ"), not(contains("hoàn lại tiền")), eq("/orders/OB-20261023-ABCDEF"));
    }

    @Test
    @DisplayName("Nhân viên huỷ đơn có dùng mã giảm giá thì lượt dùng được trả lại")
    void cancelReleasesPromotionUsage() {
        CustomerOrder order = order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);
        Promotion promotion = Promotion.builder().code("WELCOME10").build();
        promotion.setId(5L);
        order.setPromotion(promotion);

        service.cancel(ORDER_ID, null);

        verify(promotionService).releaseUsage(5L);
    }

    @Test
    @DisplayName("Huỷ đơn đã thanh toán chuyển trạng thái thanh toán sang REFUNDED")
    void cancelPaidOrderMarksRefund() {
        CustomerOrder order = order(OrderStatus.READY_FOR_PICKUP, FulfillmentType.PICKUP, PaymentMethod.BANK_TRANSFER, PaymentStatus.PAID);

        service.cancel(ORDER_ID, null);

        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261023-ABCDEF đã bị huỷ"), contains("hoàn lại tiền"), any());
    }

    @Test
    @DisplayName("Đơn đã hoàn tất hoặc đã huỷ không huỷ được, sách đã bán không bị trả về kho")
    void cancelRejectsClosedOrders() {
        CustomerOrder completed = order(OrderStatus.COMPLETED, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.PAID);
        copy1.setStatus(CopyStatus.SOLD);
        copy2.setStatus(CopyStatus.SOLD);

        assertThatThrownBy(() -> service.cancel(ORDER_ID, null)).isInstanceOf(BusinessException.class);

        assertThat(completed.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.SOLD);

        order(OrderStatus.CANCELLED, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);
        assertThatThrownBy(() -> service.cancel(ORDER_ID, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Nhân viên chi nhánh khác không xử lý được đơn, quản lý/admin (scope null) thì được")
    void staffIsScopedToOwnStore() {
        CustomerOrder order = order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);

        assertThatThrownBy(() -> service.confirm(ORDER_ID, 2L))
                .isInstanceOf(BusinessException.class).hasMessageContaining("chi nhánh mình");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        service.confirm(ORDER_ID, 1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Xem đơn của chi nhánh khác bị chặn, mã không tồn tại báo không tìm thấy")
    void getOrderChecksScopeAndExistence() {
        CustomerOrder order = buildOrder(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);
        when(customerOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(customerOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(service.getOrder(ORDER_ID, 1L).getOrderCode()).isEqualTo("OB-20261023-ABCDEF");
        assertThatThrownBy(() -> service.getOrder(ORDER_ID, 2L)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.getOrder(999L, null)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Tìm kiếm chuẩn hoá từ khoá rỗng thành null và ánh xạ sang DTO")
    void searchNormalizesKeyword() {
        CustomerOrder order = buildOrder(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentMethod.COD, PaymentStatus.UNPAID);
        PageRequest pageable = PageRequest.of(0, 10);
        when(customerOrderRepository.search(1L, OrderStatus.PENDING, null, pageable)).thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderSummaryDTO> result = service.search(1L, OrderStatus.PENDING, "   ", pageable);

        assertThat(result.getContent()).extracting(OrderSummaryDTO::getOrderCode).containsExactly("OB-20261023-ABCDEF");
        verify(customerOrderRepository).search(1L, OrderStatus.PENDING, null, pageable);
    }
}
