package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
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
import com.bookstorechain.enums.Role;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.OrderNotificationService;
import com.bookstorechain.service.UserNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
class ShipperOrderServiceImplTest {

    private static final long ORDER_ID = 50L;
    private static final long SHIPPER_ID = 30L;
    private static final long OTHER_SHIPPER_ID = 31L;
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-30T10:00:00Z"), ZoneId.of("UTC"));

    @Mock private CustomerOrderRepository customerOrderRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserNotificationService userNotificationService;
    @Mock private OrderNotificationService orderNotificationService;

    private ShipperOrderServiceImpl service;
    private Store hanoi;
    private Store saigon;
    private User shipper;
    private BookCopy copy1;
    private BookCopy copy2;

    @BeforeEach
    void setUp() {
        service = new ShipperOrderServiceImpl(customerOrderRepository, userRepository, new OrderMapper(),
                new OrderCompletionSupport(userNotificationService, CLOCK), userNotificationService, orderNotificationService);
        hanoi = TestData.store(1L, "OldBook Ha Noi");
        saigon = TestData.store(2L, "OldBook Sai Gon");
        shipper = TestData.shipper(SHIPPER_ID, hanoi);
        Book book = TestData.book(100L, "Hoang Tu Be");
        copy1 = TestData.copy(1L, "OB-HN-001", "60000", hanoi, book, CopyStatus.RESERVED);
        copy2 = TestData.copy(2L, "OB-HN-002", "50000", hanoi, book, CopyStatus.RESERVED);
        when(userRepository.findById(SHIPPER_ID)).thenReturn(Optional.of(shipper));
    }

    private CustomerOrder buildOrder(OrderStatus status, Store store, PaymentMethod method, PaymentStatus payment) {
        CustomerOrder order = CustomerOrder.builder()
                .orderCode("OB-20261030-ABCDEF").customer(TestData.customer(7L)).store(store).status(status)
                .fulfillmentType(FulfillmentType.DELIVERY).receiverName("Khach A").receiverPhone("0901234567")
                .shippingAddress("12 Pho Hue").subtotal(new BigDecimal("110000")).totalAmount(new BigDecimal("110000")).build();
        order.setId(ORDER_ID);
        for (BookCopy copy : List.of(copy1, copy2)) {
            order.getItems().add(OrderItem.builder().order(order).copy(copy).priceSnapshot(copy.getPrice())
                    .titleSnapshot("Hoang Tu Be").copyCodeSnapshot(copy.getCopyCode()).build());
        }
        order.setPayment(Payment.builder().order(order).method(method).status(payment)
                .amount(new BigDecimal("110000")).build());
        return order;
    }

    /** Dựng đơn và cho phép khoá nó để đổi trạng thái. */
    private CustomerOrder lockedOrder(OrderStatus status, Store store, PaymentMethod method, PaymentStatus payment) {
        CustomerOrder order = buildOrder(status, store, method, payment);
        when(customerOrderRepository.lockById(ORDER_ID)).thenReturn(Optional.of(order));
        return order;
    }

    private CustomerOrder shippingOrderOf(User holder) {
        CustomerOrder order = lockedOrder(OrderStatus.SHIPPING, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);
        order.setShipper(holder);
        return order;
    }

    // ---------------------------------------------------------------- tài khoản

    @Test
    @DisplayName("Tài khoản không phải SHIPPER hoặc đã khoá bị từ chối trước mọi thao tác")
    void rejectsNonShipperAndInactiveAccounts() {
        User staff = User.builder().username("staff").fullName("Staff").role(Role.STAFF).store(hanoi).isActive(true).build();
        staff.setId(9L);
        when(userRepository.findById(9L)).thenReturn(Optional.of(staff));
        assertThatThrownBy(() -> service.findAvailable(9L)).isInstanceOf(BusinessException.class);

        shipper.setActive(false);
        assertThatThrownBy(() -> service.claim(ORDER_ID, SHIPPER_ID)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(customerOrderRepository);
    }

    @Test
    @DisplayName("Shipper chưa có chi nhánh thì không xem được đơn chờ giao")
    void shipperWithoutStoreCannotListAvailable() {
        shipper.setStore(null);

        assertThatThrownBy(() -> service.findAvailable(SHIPPER_ID))
                .isInstanceOf(BusinessException.class).hasMessageContaining("chi nhánh");
    }

    // ---------------------------------------------------------------- danh sách

    @Test
    @DisplayName("Đơn chờ nhận được lấy theo chi nhánh của shipper, không theo tham số từ request")
    void findAvailableUsesShipperStore() {
        CustomerOrder waiting = buildOrder(OrderStatus.READY_FOR_DELIVERY, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);
        when(customerOrderRepository.findByStoreIdAndStatusOrderByIdAsc(1L, OrderStatus.READY_FOR_DELIVERY)).thenReturn(List.of(waiting));

        List<OrderSummaryDTO> result = service.findAvailable(SHIPPER_ID);

        assertThat(result).extracting(OrderSummaryDTO::getOrderCode).containsExactly("OB-20261030-ABCDEF");
        assertThat(result.get(0).getAmountToCollect()).isEqualByComparingTo("110000");
    }

    @Test
    @DisplayName("Đơn đang giao và lịch sử chỉ lấy theo shipper đang đăng nhập")
    void findMineAndHistoryAreScopedToShipper() {
        CustomerOrder mine = buildOrder(OrderStatus.SHIPPING, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);
        mine.setShipper(shipper);
        when(customerOrderRepository.findByShipperIdAndStatusOrderByIdAsc(SHIPPER_ID, OrderStatus.SHIPPING)).thenReturn(List.of(mine));
        when(customerOrderRepository.findTop20ByShipperIdAndStatusInOrderByIdDesc(SHIPPER_ID,
                List.of(OrderStatus.COMPLETED, OrderStatus.DELIVERY_FAILED))).thenReturn(List.of(mine));

        assertThat(service.findMine(SHIPPER_ID)).hasSize(1);
        assertThat(service.findHistory(SHIPPER_ID)).hasSize(1);
        assertThat(service.findMine(SHIPPER_ID).get(0).getShipperName()).isEqualTo("Shipper 30");
    }

    // ---------------------------------------------------------------- xem chi tiết

    @Test
    @DisplayName("Xem được đơn chờ giao của chi nhánh mình và đơn do mình giữ")
    void getOrderAllowsOwnStoreWaitingAndOwnAssigned() {
        CustomerOrder waiting = buildOrder(OrderStatus.READY_FOR_DELIVERY, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);
        when(customerOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(waiting));
        assertThat(service.getOrder(ORDER_ID, SHIPPER_ID).getOrderId()).isEqualTo(ORDER_ID);

        CustomerOrder mine = buildOrder(OrderStatus.COMPLETED, hanoi, PaymentMethod.COD, PaymentStatus.PAID);
        mine.setShipper(shipper);
        when(customerOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(mine));
        assertThat(service.getOrder(ORDER_ID, SHIPPER_ID).getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("Không xem được đơn chờ giao của chi nhánh khác hay đơn shipper khác đang giữ")
    void getOrderBlocksForeignOrders() {
        CustomerOrder otherStore = buildOrder(OrderStatus.READY_FOR_DELIVERY, saigon, PaymentMethod.COD, PaymentStatus.UNPAID);
        when(customerOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(otherStore));
        assertThatThrownBy(() -> service.getOrder(ORDER_ID, SHIPPER_ID)).isInstanceOf(BusinessException.class);

        CustomerOrder heldByOther = buildOrder(OrderStatus.SHIPPING, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);
        heldByOther.setShipper(TestData.shipper(OTHER_SHIPPER_ID, hanoi));
        when(customerOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(heldByOther));
        assertThatThrownBy(() -> service.getOrder(ORDER_ID, SHIPPER_ID)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Đơn không tồn tại báo không tìm thấy")
    void getOrderNotFound() {
        when(customerOrderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(ORDER_ID, SHIPPER_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------------- nhận đơn

    @Test
    @DisplayName("Nhận đơn: gán shipper, sang SHIPPING và báo khách tên shipper")
    void claimAssignsShipper() {
        CustomerOrder order = lockedOrder(OrderStatus.READY_FOR_DELIVERY, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);

        service.claim(ORDER_ID, SHIPPER_ID);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(order.getShipper()).isSameAs(shipper);
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261030-ABCDEF đang được giao"), contains("Shipper 30"), eq("/orders/OB-20261030-ABCDEF"));
        verify(orderNotificationService).notifyDeliveryTaken(any());
        verify(orderNotificationService).notifyStaffDelivery(any(), eq("Shipper đã nhận đơn OB-20261030-ABCDEF"), contains("Shipper 30"));
    }

    @Test
    @DisplayName("Đơn đã có shipper khác nhận thì người bấm sau bị từ chối và đơn không đổi")
    void claimRejectsAlreadyTakenOrder() {
        CustomerOrder order = lockedOrder(OrderStatus.SHIPPING, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);
        User first = TestData.shipper(OTHER_SHIPPER_ID, hanoi);
        order.setShipper(first);

        assertThatThrownBy(() -> service.claim(ORDER_ID, SHIPPER_ID))
                .isInstanceOf(BusinessException.class).hasMessageContaining("shipper khác");

        assertThat(order.getShipper()).isSameAs(first);
        verifyNoInteractions(userNotificationService);
        verifyNoInteractions(orderNotificationService);
    }

    @Test
    @DisplayName("Không nhận được đơn của chi nhánh khác hoặc đơn chưa đóng gói")
    void claimRejectsOtherStoreAndWrongStatus() {
        CustomerOrder foreign = lockedOrder(OrderStatus.READY_FOR_DELIVERY, saigon, PaymentMethod.COD, PaymentStatus.UNPAID);
        assertThatThrownBy(() -> service.claim(ORDER_ID, SHIPPER_ID)).isInstanceOf(BusinessException.class);
        assertThat(foreign.getShipper()).isNull();
        assertThat(foreign.getStatus()).isEqualTo(OrderStatus.READY_FOR_DELIVERY);

        CustomerOrder notReady = lockedOrder(OrderStatus.CONFIRMED, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);
        assertThatThrownBy(() -> service.claim(ORDER_ID, SHIPPER_ID)).isInstanceOf(BusinessException.class);
        assertThat(notReady.getShipper()).isNull();
        verifyNoInteractions(userNotificationService);
    }

    // ---------------------------------------------------------------- đã giao

    @Test
    @DisplayName("Giao thành công đơn COD: sách thành SOLD, ghi nhận đã thu tiền, tính doanh thu theo ngày giao")
    void deliverCompletesCodOrder() {
        CustomerOrder order = shippingOrderOf(shipper);

        service.deliver(ORDER_ID, SHIPPER_ID);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.SOLD);
        assertThat(copy2.getStatus()).isEqualTo(CopyStatus.SOLD);
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getCompletedAt()).isEqualTo(LocalDateTime.of(2026, 10, 30, 10, 0));
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261030-ABCDEF đã hoàn tất"), contains("đánh giá"), any());
    }

    @Test
    @DisplayName("Đơn chuyển khoản chưa được chi nhánh xác nhận tiền thì shipper chưa chốt được")
    void deliverRequiresConfirmedBankTransfer() {
        CustomerOrder order = lockedOrder(OrderStatus.SHIPPING, hanoi, PaymentMethod.BANK_TRANSFER, PaymentStatus.UNPAID);
        order.setShipper(shipper);

        assertThatThrownBy(() -> service.deliver(ORDER_ID, SHIPPER_ID))
                .isInstanceOf(BusinessException.class).hasMessageContaining("chuyển khoản");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.RESERVED);
    }

    @Test
    @DisplayName("Shipper khác không chốt được đơn của người khác; đơn không ở SHIPPING cũng bị từ chối")
    void deliverRejectsForeignAndWrongStatus() {
        CustomerOrder held = shippingOrderOf(TestData.shipper(OTHER_SHIPPER_ID, hanoi));
        assertThatThrownBy(() -> service.deliver(ORDER_ID, SHIPPER_ID)).isInstanceOf(BusinessException.class);
        assertThat(held.getStatus()).isEqualTo(OrderStatus.SHIPPING);

        CustomerOrder done = lockedOrder(OrderStatus.COMPLETED, hanoi, PaymentMethod.COD, PaymentStatus.PAID);
        done.setShipper(shipper);
        assertThatThrownBy(() -> service.deliver(ORDER_ID, SHIPPER_ID)).isInstanceOf(BusinessException.class);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.RESERVED);
        verifyNoInteractions(userNotificationService);
    }

    // ---------------------------------------------------------------- giao thất bại

    @Test
    @DisplayName("Giao thất bại: lưu lý do đã cắt khoảng trắng, giữ sách RESERVED, báo khách mà không lộ lý do")
    void failRecordsReasonAndKeepsCopiesReserved() {
        CustomerOrder order = shippingOrderOf(shipper);

        service.fail(ORDER_ID, SHIPPER_ID, "  Khach khong nghe may  ");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERY_FAILED);
        assertThat(order.getDeliveryFailureReason()).isEqualTo("Khach khong nghe may");
        assertThat(order.getShipper()).as("giữ shipper để còn trong lịch sử").isSameAs(shipper);
        assertThat(copy1.getStatus()).isEqualTo(CopyStatus.RESERVED);
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.UNPAID);
        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.ORDER),
                eq("Đơn OB-20261030-ABCDEF chưa giao được"), not(contains("nghe may")), any());
        verify(orderNotificationService).notifyStaffDelivery(any(), eq("Giao thất bại đơn OB-20261030-ABCDEF"), not(contains("nghe may")));
    }

    @Test
    @DisplayName("Lý do để trống hoặc quá dài bị từ chối và đơn không đổi")
    void failRequiresReasonWithinLimit() {
        // Lý do được kiểm tra trước khi khoá đơn nên không cần stub lockById
        CustomerOrder order = buildOrder(OrderStatus.SHIPPING, hanoi, PaymentMethod.COD, PaymentStatus.UNPAID);
        order.setShipper(shipper);

        assertThatThrownBy(() -> service.fail(ORDER_ID, SHIPPER_ID, "   ")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.fail(ORDER_ID, SHIPPER_ID, null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.fail(ORDER_ID, SHIPPER_ID, "x".repeat(256)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("255");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(order.getDeliveryFailureReason()).isNull();
    }

    @Test
    @DisplayName("Shipper khác không báo thất bại thay được đơn của người khác")
    void failRejectsForeignOrder() {
        CustomerOrder held = shippingOrderOf(TestData.shipper(OTHER_SHIPPER_ID, hanoi));

        assertThatThrownBy(() -> service.fail(ORDER_ID, SHIPPER_ID, "Ly do")).isInstanceOf(BusinessException.class);

        assertThat(held.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }
}
