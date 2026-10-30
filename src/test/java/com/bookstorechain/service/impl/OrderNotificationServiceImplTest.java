package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.NotificationDTO;
import com.bookstorechain.dto.response.OrderLineDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.enums.FulfillmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderNotificationServiceImplTest {

    @Mock private SimpMessagingTemplate messagingTemplate;

    private OrderSummaryDTO order() {
        return OrderSummaryDTO.builder()
                .orderId(77L).orderCode("OB-20261023-ABCDEF").storeId(3L).storeName("OldBook Da Nang")
                .customerName("Nguyen Van Rieng Tu").receiverPhone("0911222333").shippingAddress("99 Duong Bi Mat")
                .fulfillmentType(FulfillmentType.DELIVERY).totalAmount(new BigDecimal("190000"))
                .lines(List.of(new OrderLineDTO(), new OrderLineDTO())).build();
    }

    @Test
    @DisplayName("Đơn mới được gửi tới kênh chi nhánh và kênh của quản trị viên")
    void sendsToStoreTopicAndAllStoresTopic() {
        new OrderNotificationServiceImpl(messagingTemplate).notifyNewOrder(order());

        ArgumentCaptor<String> destinations = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(2)).convertAndSend(destinations.capture(), payloads.capture());

        assertThat(destinations.getAllValues()).containsExactlyInAnyOrder("/topic/store/3", "/topic/store/all");
        NotificationDTO sent = (NotificationDTO) payloads.getAllValues().get(0);
        assertThat(sent.getType()).isEqualTo("ORDER");
        assertThat(sent.getTitle()).contains("OB-20261023-ABCDEF");
        assertThat(sent.getMessage()).contains("2 cuốn", "190,000 đ", "Giao tận nơi", "OldBook Da Nang");
        assertThat(sent.getTargetUrl()).isEqualTo("/admin/orders/77");
        assertThat(sent.getStoreId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Thông báo không chứa tên, số điện thoại hay địa chỉ của khách")
    void doesNotLeakCustomerPersonalData() {
        new OrderNotificationServiceImpl(messagingTemplate).notifyNewOrder(order());

        ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), payloads.capture());

        NotificationDTO sent = (NotificationDTO) payloads.getAllValues().get(0);
        String everything = sent.getTitle() + " " + sent.getMessage() + " " + sent.getTargetUrl();
        assertThat(everything).doesNotContain("Rieng Tu", "0911222333", "Bi Mat");
    }

    private OrderSummaryDTO codDeliveryOrder() {
        OrderSummaryDTO order = order();
        order.setPaymentMethod(com.bookstorechain.enums.PaymentMethod.COD);
        order.setPaymentStatus(com.bookstorechain.enums.PaymentStatus.UNPAID);
        return order;
    }

    @Test
    @DisplayName("Đơn chờ giao chỉ gửi tới kênh shipper của đúng chi nhánh, kèm số tiền cần thu, không lộ thông tin khách")
    void deliveryAvailableGoesToShipperTopicOnly() {
        new OrderNotificationServiceImpl(messagingTemplate).notifyDeliveryAvailable(codDeliveryOrder());

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/shipper/store/3"), payload.capture());

        NotificationDTO sent = (NotificationDTO) payload.getValue();
        assertThat(sent.getType()).isEqualTo("DELIVERY_AVAILABLE");
        assertThat(sent.getMessage()).contains("2 cuốn", "Thu 190,000 đ", "OldBook Da Nang");
        assertThat(sent.getTargetUrl()).isEqualTo("/shipper/orders/77");
        assertThat(sent.getTitle() + sent.getMessage() + sent.getTargetUrl()).doesNotContain("Rieng Tu", "0911222333", "Bi Mat");
    }

    @Test
    @DisplayName("Đơn đã thanh toán trước thì thông báo cho shipper ghi Đã thanh toán, không có số tiền thu")
    void deliveryAvailableForPaidOrderShowsNoCollection() {
        OrderSummaryDTO paid = codDeliveryOrder();
        paid.setPaymentStatus(com.bookstorechain.enums.PaymentStatus.PAID);

        new OrderNotificationServiceImpl(messagingTemplate).notifyDeliveryAvailable(paid);

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(anyString(), payload.capture());
        assertThat(((NotificationDTO) payload.getValue()).getMessage()).contains("Đã thanh toán").doesNotContain("Thu ");
    }

    @Test
    @DisplayName("Đơn đã có người nhận gửi loại DELIVERY_TAKEN tới kênh shipper để làm mới danh sách")
    void deliveryTakenGoesToShipperTopic() {
        new OrderNotificationServiceImpl(messagingTemplate).notifyDeliveryTaken(order());

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/shipper/store/3"), payload.capture());
        assertThat(((NotificationDTO) payload.getValue()).getType()).isEqualTo("DELIVERY_TAKEN");
    }

    @Test
    @DisplayName("Diễn biến giao hàng gửi tới kênh chi nhánh và kênh quản trị, dẫn tới trang đơn của admin")
    void staffDeliveryGoesToStaffTopics() {
        new OrderNotificationServiceImpl(messagingTemplate).notifyStaffDelivery(order(), "Giao thất bại đơn X", "Cần xử lý");

        ArgumentCaptor<String> destinations = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(2)).convertAndSend(destinations.capture(), payloads.capture());
        assertThat(destinations.getAllValues()).containsExactlyInAnyOrder("/topic/store/3", "/topic/store/all");
        NotificationDTO sent = (NotificationDTO) payloads.getAllValues().get(0);
        assertThat(sent.getType()).isEqualTo("DELIVERY");
        assertThat(sent.getTitle()).isEqualTo("Giao thất bại đơn X");
        assertThat(sent.getTargetUrl()).isEqualTo("/admin/orders/77");
    }

    @Test
    @DisplayName("Trong transaction chỉ gửi sau khi commit, huỷ transaction thì không gửi")
    void sendsOnlyAfterCommit() {
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        try {
            new OrderNotificationServiceImpl(messagingTemplate).notifyDeliveryAvailable(order());
            org.mockito.Mockito.verifyNoInteractions(messagingTemplate);

            for (org.springframework.transaction.support.TransactionSynchronization sync
                    : org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }
            verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/shipper/store/3"), any(Object.class));
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("Lỗi WebSocket không làm hỏng việc đặt hàng đã hoàn tất")
    void swallowsMessagingFailures() {
        doThrow(new IllegalStateException("broker down")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        assertThatCode(() -> new OrderNotificationServiceImpl(messagingTemplate).notifyNewOrder(order())).doesNotThrowAnyException();
    }
}
