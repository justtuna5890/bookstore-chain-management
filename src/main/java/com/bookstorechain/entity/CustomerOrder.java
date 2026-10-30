package com.bookstorechain.entity;

import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_order") // "order" là từ khoá SQL nên đặt tên bảng khác
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrder extends BaseEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 30, columnDefinition = "NVARCHAR(30)")
    private String orderCode; // VD: OB-20261023-0001

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // Mỗi đơn thuộc đúng một chi nhánh

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private FulfillmentType fulfillmentType;

    @Column(name = "receiver_name", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private String receiverPhone;

    @Column(name = "shipping_address", columnDefinition = "NVARCHAR(255)")
    private String shippingAddress; // Chỉ dùng khi giao tận nơi

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** Shipper đang giữ đơn (null khi đơn chưa có người nhận, đã về chi nhánh hoặc nhận tại cửa hàng). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    /** Lý do giao thất bại do shipper ghi; xoá khi nhân viên cho giao lại. */
    @Column(name = "delivery_failure_reason", columnDefinition = "NVARCHAR(255)")
    private String deliveryFailureReason;

    /** Thời điểm đơn hoàn tất; doanh thu được tính theo ngày này, không theo ngày đặt hay ngày sửa cuối. */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Mã giảm giá đã dùng cho đơn (null nếu không dùng) và bản sao mã để hiển thị dù mã sau này bị sửa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "promotion_code", length = 30, columnDefinition = "NVARCHAR(30)")
    private String promotionCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;
}
