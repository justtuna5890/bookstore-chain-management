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
    /** Kênh của các shipper thuộc một chi nhánh; WebSocketSubscriptionInterceptor chỉ cho shipper đúng chi nhánh đăng ký. */
    static final String SHIPPER_TOPIC_PREFIX = "/topic/shipper/store/";

    /** Diễn biến giao hàng gửi cho nhân viên; nav-dock dùng loại này để đặt nhãn "Giao Hàng" cho cửa sổ thông báo. */
    static final String TYPE_DELIVERY = "DELIVERY";
    static final String TYPE_DELIVERY_AVAILABLE = "DELIVERY_AVAILABLE";
    static final String TYPE_DELIVERY_TAKEN = "DELIVERY_TAKEN";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyNewOrder(OrderSummaryDTO order) {
        safely(order, () -> {
            // Kênh này chưa xác thực người đăng ký nên nội dung chỉ gồm mã đơn, số cuốn, tiền và hình thức nhận,
            // không có tên, số điện thoại hay địa chỉ của khách
            publishToStaff(order, build(order, "Đơn hàng mới (" + order.getOrderCode() + ")",
                    order.getItemCount() + " cuốn · " + formatMoney(order.getTotalAmount().longValue()) + " · "
                            + (order.getFulfillmentType() == FulfillmentType.DELIVERY ? "Giao tận nơi" : "Nhận tại chi nhánh")
                            + " · " + order.getStoreName(),
                    "ORDER", "/admin/orders/" + order.getOrderId()));
        });
    }

    @Override
    public void notifyDeliveryAvailable(OrderSummaryDTO order) {
        safely(order, () -> {
            String collect = order.getAmountToCollect().signum() > 0
                    ? " · Thu " + formatMoney(order.getAmountToCollect().longValue()) : " · Đã thanh toán";
            publish(order, SHIPPER_TOPIC_PREFIX + order.getStoreId(),
                    build(order, "Có đơn chờ giao (" + order.getOrderCode() + ")",
                            order.getItemCount() + " cuốn" + collect + " · " + order.getStoreName(),
                            TYPE_DELIVERY_AVAILABLE, "/shipper/orders/" + order.getOrderId()));
        });
    }

    @Override
    public void notifyDeliveryTaken(OrderSummaryDTO order) {
        safely(order, () -> publish(order, SHIPPER_TOPIC_PREFIX + order.getStoreId(),
                build(order, "Đơn " + order.getOrderCode() + " đã có người nhận", order.getStoreName(),
                        TYPE_DELIVERY_TAKEN, "/shipper/orders")));
    }

    @Override
    public void notifyStaffDelivery(OrderSummaryDTO order, String title, String message) {
        safely(order, () -> publishToStaff(order,
                build(order, title, message, TYPE_DELIVERY, "/admin/orders/" + order.getOrderId())));
    }

    private void publishToStaff(OrderSummaryDTO order, NotificationDTO notification) {
        publish(order, STORE_TOPIC_PREFIX + order.getStoreId(), notification);
        publish(order, ALL_STORES_TOPIC, notification);
    }

    /** Gửi sau khi transaction của người gọi commit; lỗi gửi chỉ được ghi log. */
    private void publish(OrderSummaryDTO order, String destination, NotificationDTO notification) {
        AfterCommit.run(() -> {
            try {
                messagingTemplate.convertAndSend(destination, notification);
                log.info("Đã phát thông báo WebSocket {} cho đơn hàng {} tới {}", notification.getType(), order.getOrderCode(), destination);
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo WebSocket đơn hàng {}", order.getOrderCode(), e);
            }
        });
    }

    /** Dựng nội dung hoặc đăng ký gửi bị lỗi cũng không được làm hỏng nghiệp vụ đang gọi. */
    private void safely(OrderSummaryDTO order, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo WebSocket đơn hàng {}", order.getOrderCode(), e);
        }
    }

    private NotificationDTO build(OrderSummaryDTO order, String title, String message, String type, String targetUrl) {
        return NotificationDTO.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .message(message)
                .type(type)
                .storeId(order.getStoreId())
                .storeName(order.getStoreName())
                .targetUrl(targetUrl)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String formatMoney(long amount) {
        return String.format(Locale.US, "%,d", amount) + " đ";
    }
}
