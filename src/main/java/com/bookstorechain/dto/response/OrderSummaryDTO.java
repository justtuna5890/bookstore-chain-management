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
    private String promotionCode;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    /** Shipper đang giữ đơn (null nếu chưa có). */
    private Long shipperId;
    private String shipperName;
    private String deliveryFailureReason;
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
        return status.getLabel();
    }

    /** Lớp Tailwind cho huy hiệu trạng thái. */
    public String getStatusClasses() {
        switch (status) {
            case PENDING: return "bg-amber-500/15 border-amber-500/30 text-amber-300";
            case CONFIRMED: return "bg-sky-500/15 border-sky-500/30 text-sky-300";
            case READY_FOR_PICKUP:
            case READY_FOR_DELIVERY:
            case SHIPPING: return "bg-violet-500/15 border-violet-500/30 text-violet-300";
            case DELIVERY_FAILED: return "bg-orange-500/15 border-orange-500/30 text-orange-300";
            case COMPLETED: return "bg-emerald-500/15 border-emerald-500/30 text-emerald-300";
            default: return "bg-rose-500/15 border-rose-500/30 text-rose-300";
        }
    }

    /** Số tiền shipper phải thu tận tay: chỉ đơn thanh toán khi nhận hàng và chưa thu. */
    public BigDecimal getAmountToCollect() {
        return paymentMethod == PaymentMethod.COD && paymentStatus == PaymentStatus.UNPAID ? totalAmount : BigDecimal.ZERO;
    }

    public String getPaymentLabel() {
        String method = paymentMethod == PaymentMethod.COD ? "Tiền mặt khi nhận hàng" : "Chuyển khoản";
        return method + (paymentStatus == PaymentStatus.PAID ? " · Đã thanh toán"
                : paymentStatus == PaymentStatus.REFUNDED ? " · Đã hoàn tiền" : " · Chưa thanh toán");
    }

    public String getPaymentMethodLabel() {
        return paymentMethod == PaymentMethod.COD ? "Tiền mặt khi nhận hàng" : "Chuyển khoản";
    }

    public String getPaymentStatusLabel() {
        return paymentStatus == PaymentStatus.PAID ? "Đã thanh toán"
                : paymentStatus == PaymentStatus.REFUNDED ? "Đã hoàn tiền" : "Chưa thanh toán";
    }

    public String getPaymentStatusClasses() {
        return paymentStatus == PaymentStatus.PAID ? "bg-emerald-500/15 border-emerald-500/30 text-emerald-300"
                : paymentStatus == PaymentStatus.REFUNDED ? "bg-slate-500/15 border-slate-500/30 text-slate-300"
                : "bg-amber-500/15 border-amber-500/30 text-amber-300";
    }

    public String getFulfillmentLabel() {
        return fulfillmentType == FulfillmentType.DELIVERY ? "Giao tận nơi" : "Nhận tại chi nhánh";
    }

    /** Các mốc xử lý đơn (không gồm trạng thái huỷ) để hiển thị tiến trình. */
    public List<TimelineStep> getTimeline() {
        OrderStatus middle = fulfillmentType == FulfillmentType.DELIVERY ? OrderStatus.SHIPPING : OrderStatus.READY_FOR_PICKUP;
        OrderStatus[] steps = {OrderStatus.PENDING, OrderStatus.CONFIRMED, middle, OrderStatus.COMPLETED};
        String[] labels = {"Đã đặt hàng", "Đã xác nhận",
                fulfillmentType == FulfillmentType.DELIVERY ? "Đang giao" : "Sẵn sàng nhận", "Hoàn tất"};
        // Mốc đã đạt: thứ tự trong chuỗi xử lý, bỏ qua nhánh không áp dụng (giao hàng hoặc nhận tại cửa hàng)
        // Đơn chờ shipper nhận vẫn dừng ở "Đã xác nhận"; mốc "Đang giao" chỉ sáng khi đã có shipper cầm đơn
        int reached = status == OrderStatus.CANCELLED ? -1
                : status == OrderStatus.PENDING ? 0
                : status == OrderStatus.CONFIRMED || status == OrderStatus.READY_FOR_DELIVERY ? 1
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
