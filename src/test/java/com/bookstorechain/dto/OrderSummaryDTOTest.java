package com.bookstorechain.dto;

import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSummaryDTOTest {

    private OrderSummaryDTO order(OrderStatus status, FulfillmentType fulfillment, PaymentStatus payment) {
        return OrderSummaryDTO.builder().status(status).fulfillmentType(fulfillment).paymentStatus(payment).build();
    }

    private List<Boolean> doneFlags(OrderSummaryDTO order) {
        return order.getTimeline().stream().map(OrderSummaryDTO.TimelineStep::isDone).toList();
    }

    @Test
    @DisplayName("Chỉ đơn PENDING chưa thanh toán mới được khách tự huỷ")
    void cancellableOnlyWhenPendingAndUnpaid() {
        assertThat(order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentStatus.UNPAID).isCancellable()).isTrue();
        assertThat(order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentStatus.PAID).isCancellable()).isFalse();
        assertThat(order(OrderStatus.CONFIRMED, FulfillmentType.PICKUP, PaymentStatus.UNPAID).isCancellable()).isFalse();
        assertThat(order(OrderStatus.CANCELLED, FulfillmentType.PICKUP, PaymentStatus.UNPAID).isCancellable()).isFalse();
    }

    @Test
    @DisplayName("Đơn nhận tại cửa hàng: mốc thứ ba là 'Sẵn sàng nhận'")
    void pickupTimelineUsesReadyStep() {
        OrderSummaryDTO pending = order(OrderStatus.PENDING, FulfillmentType.PICKUP, PaymentStatus.UNPAID);

        assertThat(pending.getTimeline()).extracting(OrderSummaryDTO.TimelineStep::getLabel)
                .containsExactly("Đã đặt hàng", "Đã xác nhận", "Sẵn sàng nhận", "Hoàn tất");
        assertThat(doneFlags(pending)).containsExactly(true, false, false, false);
        assertThat(pending.getTimeline().get(0).isCurrent()).isTrue();
    }

    @Test
    @DisplayName("Đơn giao tận nơi: mốc thứ ba là 'Đang giao' và tiến trình đạt đúng mốc")
    void deliveryTimelineProgresses() {
        OrderSummaryDTO shipping = order(OrderStatus.SHIPPING, FulfillmentType.DELIVERY, PaymentStatus.UNPAID);

        assertThat(shipping.getTimeline().get(2).getLabel()).isEqualTo("Đang giao");
        assertThat(doneFlags(shipping)).containsExactly(true, true, true, false);
        assertThat(shipping.getTimeline().get(2).isCurrent()).isTrue();

        OrderSummaryDTO confirmed = order(OrderStatus.CONFIRMED, FulfillmentType.DELIVERY, PaymentStatus.UNPAID);
        assertThat(doneFlags(confirmed)).containsExactly(true, true, false, false);
    }

    @Test
    @DisplayName("Đơn hoàn tất đạt mọi mốc, đơn đã huỷ không đạt mốc nào")
    void completedAndCancelledTimelines() {
        assertThat(doneFlags(order(OrderStatus.COMPLETED, FulfillmentType.PICKUP, PaymentStatus.PAID)))
                .containsExactly(true, true, true, true);
        assertThat(doneFlags(order(OrderStatus.CANCELLED, FulfillmentType.PICKUP, PaymentStatus.UNPAID)))
                .containsExactly(false, false, false, false);
    }
}
