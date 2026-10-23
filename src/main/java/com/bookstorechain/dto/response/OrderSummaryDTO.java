package com.bookstorechain.dto.response;

import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryDTO {
    private Long orderId;
    private String orderCode;
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private String storePhone;
    /** Tên người nhận hàng ghi trên đơn. */
    private String customerName;
    private String receiverPhone;
    private String shippingAddress;
    private String note;
    private OrderStatus status;
    private FulfillmentType fulfillmentType;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionCode;
    private LocalDateTime paidAt;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderLineDTO> lines;

    public String getFormattedPaidAt() {
        return paidAt == null ? "" : paidAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public int getItemCount() {
        return lines == null ? 0 : lines.size();
    }

    public String getFormattedCreatedAt() {
        return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /** Khách chỉ tự huỷ được đơn chưa xác nhận và chưa thanh toán. */
    public boolean isCancellable() {
        return status == OrderStatus.PENDING && paymentStatus == PaymentStatus.UNPAID;
    }

    public String getStatusLabel() {
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case READY_FOR_PICKUP: return "Sẵn sàng nhận hàng";
            case SHIPPING: return "Đang giao";
            case COMPLETED: return "Hoàn tất";
            case CANCELLED: return "Đã huỷ";
            default: return status.name();
        }
    }

    /** Lớp Tailwind cho huy hiệu trạng thái. */
    public String getStatusClasses() {
        switch (status) {
            case PENDING: return "bg-amber-500/15 border-amber-500/30 text-amber-300";
            case CONFIRMED: return "bg-sky-500/15 border-sky-500/30 text-sky-300";
            case READY_FOR_PICKUP:
            case SHIPPING: return "bg-violet-500/15 border-violet-500/30 text-violet-300";
            case COMPLETED: return "bg-emerald-500/15 border-emerald-500/30 text-emerald-300";
            default: return "bg-rose-500/15 border-rose-500/30 text-rose-300";
        }
    }

    public String getPaymentLabel() {
        String method = paymentMethod == PaymentMethod.COD ? "Tiền mặt khi nhận hàng" : "Chuyển khoản";
        return method + (paymentStatus == PaymentStatus.PAID ? " · Đã thanh toán"
                : paymentStatus == PaymentStatus.REFUNDED ? " · Đã hoàn tiền" : " · Chưa thanh toán");
    }

    /** Các mốc xử lý đơn (không gồm trạng thái huỷ) để hiển thị tiến trình. */
    public List<TimelineStep> getTimeline() {
        OrderStatus middle = fulfillmentType == FulfillmentType.DELIVERY ? OrderStatus.SHIPPING : OrderStatus.READY_FOR_PICKUP;
        OrderStatus[] steps = {OrderStatus.PENDING, OrderStatus.CONFIRMED, middle, OrderStatus.COMPLETED};
        String[] labels = {"Đã đặt hàng", "Đã xác nhận",
                fulfillmentType == FulfillmentType.DELIVERY ? "Đang giao" : "Sẵn sàng nhận", "Hoàn tất"};
        // Mốc đã đạt: thứ tự trong chuỗi xử lý, bỏ qua nhánh không áp dụng (giao hàng hoặc nhận tại cửa hàng)
        int reached = status == OrderStatus.CANCELLED ? -1
                : status == OrderStatus.PENDING ? 0
                : status == OrderStatus.CONFIRMED ? 1
                : status == OrderStatus.COMPLETED ? 3 : 2;
        List<TimelineStep> timeline = new ArrayList<>();
        for (int i = 0; i < steps.length; i++) {
            timeline.add(new TimelineStep(labels[i], i <= reached, i == reached));
        }
        return timeline;
    }

    @Getter
    @AllArgsConstructor
    public static class TimelineStep {
        private final String label;
        private final boolean done;
        private final boolean current;
    }
}
