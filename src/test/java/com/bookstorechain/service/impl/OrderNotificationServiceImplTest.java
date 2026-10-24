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

    @Test
    @DisplayName("Lỗi WebSocket không làm hỏng việc đặt hàng đã hoàn tất")
    void swallowsMessagingFailures() {
        doThrow(new IllegalStateException("broker down")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        assertThatCode(() -> new OrderNotificationServiceImpl(messagingTemplate).notifyNewOrder(order())).doesNotThrowAnyException();
    }
}
