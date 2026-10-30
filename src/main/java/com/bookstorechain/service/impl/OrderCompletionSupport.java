package com.bookstorechain.service.impl;

import com.bookstorechain.entity.CustomerOrder;
import com.bookstorechain.entity.OrderItem;
import com.bookstorechain.entity.Payment;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.enums.PaymentStatus;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Bước chốt đơn dùng chung: nhân viên chốt đơn nhận tại cửa hàng, shipper chốt đơn giao tận nơi.
 * Người gọi phải đã khoá dòng đơn và kiểm tra trạng thái trước; phương thức này chạy trong transaction của người gọi.
 */
@Component
@RequiredArgsConstructor
public class OrderCompletionSupport {

    private final UserNotificationService userNotificationService;
    private final Clock clock;

    public void complete(CustomerOrder order) {
        // Kiểm tra hết trước khi sửa bất cứ thứ gì để không để lại đơn bán dở
        for (OrderItem item : order.getItems()) {
            if (item.getCopy().getStatus() != CopyStatus.RESERVED) {
                throw new BusinessException("Bản sách " + item.getCopyCodeSnapshot()
                        + " không còn ở trạng thái giữ cho đơn này. Hãy kiểm tra lại kho trước khi hoàn tất.");
            }
        }
        Payment payment = order.getPayment();
        if (payment.getStatus() == PaymentStatus.UNPAID) {
            if (payment.getMethod() != PaymentMethod.COD) {
                throw new BusinessException("Chưa xác nhận đã nhận tiền chuyển khoản. Chi nhánh cần xác nhận thanh toán trước khi hoàn tất đơn.");
            }
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now(clock));
        }
        order.getItems().forEach(item -> item.getCopy().setStatus(CopyStatus.SOLD));
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now(clock));
        userNotificationService.notify(order.getCustomer().getId(), UserNotificationType.ORDER,
                "Đơn " + order.getOrderCode() + " đã hoàn tất",
                "Cảm ơn bạn đã mua sách. Hãy để lại đánh giá cho những cuốn bạn vừa nhận.",
                "/orders/" + order.getOrderCode());
    }
}
