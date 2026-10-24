package com.bookstorechain.dto.response;

import com.bookstorechain.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDTO {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private String code;
    private String description;
    private PromotionType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer usageLimit;
    private int usedCount;
    private boolean onePerCustomer;
    private Long storeId;
    private String storeName;
    private boolean active;
    /** Trạng thái tính tại thời điểm xem: RUNNING, UPCOMING, EXPIRED, EXHAUSTED hoặc PAUSED. */
    private String status;

    public boolean isRunning() {
        return "RUNNING".equals(status);
    }

    public String getStatusLabel() {
        switch (status) {
            case "RUNNING": return "Đang chạy";
            case "UPCOMING": return "Sắp diễn ra";
            case "EXPIRED": return "Hết hạn";
            case "EXHAUSTED": return "Hết lượt";
            default: return "Tạm dừng";
        }
    }

    public String getStatusClasses() {
        switch (status) {
            case "RUNNING": return "bg-emerald-500/15 border-emerald-500/30 text-emerald-300";
            case "UPCOMING": return "bg-sky-500/15 border-sky-500/30 text-sky-300";
            case "PAUSED": return "bg-slate-500/15 border-slate-500/30 text-slate-300";
            default: return "bg-rose-500/15 border-rose-500/30 text-rose-300";
        }
    }

    /** Mức giảm để hiển thị, VD "10%" hoặc "20,000 đ". */
    public String getValueLabel() {
        if (type == PromotionType.PERCENT) {
            return value.stripTrailingZeros().toPlainString() + "%";
        }
        return formatMoney(value);
    }

    public String getFormattedStartsAt() {
        return startsAt == null ? "" : startsAt.format(DATE_TIME);
    }

    public String getFormattedEndsAt() {
        return endsAt == null ? "" : endsAt.format(DATE_TIME);
    }

    public String getScopeLabel() {
        return storeId == null ? "Toàn chuỗi" : storeName;
    }

    /** Lượt còn lại; null nếu không giới hạn. */
    public Integer getRemainingUses() {
        return usageLimit == null ? null : Math.max(usageLimit - usedCount, 0);
    }

    private static String formatMoney(BigDecimal amount) {
        return String.format(Locale.US, "%,d", amount.longValue()) + " đ";
    }
}
