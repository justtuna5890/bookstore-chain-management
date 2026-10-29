package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.NotificationDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationServiceImpl implements OrderNotificationService {

    /** Kênh của nhân viên và quản lý thuộc một chi nhánh (đã được nav-dock đăng ký theo chi nhánh của người dùng). */
    static final String STORE_TOPIC_PREFIX = "/topic/store/";
    /** Kênh của quản trị viên chưa gán chi nhánh, nhận đơn mới của mọi chi nhánh. */
    static final String ALL_STORES_TOPIC = "/topic/store/all";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyNewOrder(OrderSummaryDTO order) {
        try {
            // Kênh này chưa xác thực người đăng ký nên nội dung chỉ gồm mã đơn, số cuốn, tiền và hình thức nhận,
            // không có tên, số điện thoại hay địa chỉ của khách
            NotificationDTO notification = NotificationDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Đơn hàng mới (" + order.getOrderCode() + ")")
                    .message(order.getItemCount() + " cuốn · " + formatMoney(order) + " · "
                            + (order.getFulfillmentType() == FulfillmentType.DELIVERY ? "Giao tận nơi" : "Nhận tại chi nhánh")
                            + " · " + order.getStoreName())
                    .type("ORDER")
                    .storeId(order.getStoreId())
                    .storeName(order.getStoreName())
                    .targetUrl("/admin/orders/" + order.getOrderId())
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend(STORE_TOPIC_PREFIX + order.getStoreId(), notification);
            messagingTemplate.convertAndSend(ALL_STORES_TOPIC, notification);
            log.info("Đã phát thông báo WebSocket cho đơn hàng mới {}", order.getOrderCode());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo WebSocket đơn hàng {}", order.getOrderCode(), e);
        }
    }

    private String formatMoney(OrderSummaryDTO order) {
        return String.format(Locale.US, "%,d", order.getTotalAmount().longValue()) + " đ";
    }
}
