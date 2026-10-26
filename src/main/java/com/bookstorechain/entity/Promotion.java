package com.bookstorechain.entity;

import com.bookstorechain.enums.PromotionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30, columnDefinition = "NVARCHAR(30)")
    private String code; // Luôn lưu chữ hoa, VD: WELCOME10

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private PromotionType type;

    /** PERCENT: phần trăm (1-100); FIXED: số tiền đồng. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "min_order_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    /** Mức giảm tối đa, chỉ có ý nghĩa với PERCENT; null là không giới hạn. */
    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    /** Tổng số đơn được dùng mã; null là không giới hạn. */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /** Số đơn đang dùng mã (đơn bị huỷ được trả lại lượt). */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private int usedCount = 0;

    @Column(name = "one_per_customer", nullable = false)
    @Builder.Default
    private boolean onePerCustomer = true;

    /** Chi nhánh được áp dụng; null là toàn chuỗi. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
