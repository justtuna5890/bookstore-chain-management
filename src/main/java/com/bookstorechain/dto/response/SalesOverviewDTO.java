package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Các chỉ số bán hàng online hiển thị đầu trang dashboard. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOverviewDTO {
    @Builder.Default
    private BigDecimal revenueToday = BigDecimal.ZERO;
    private long ordersToday;
    @Builder.Default
    private BigDecimal revenueMonth = BigDecimal.ZERO;
    private long ordersMonth;
    /** Doanh thu cùng kỳ tháng trước (từ ngày 1 đến cùng ngày của tháng trước). */
    @Builder.Default
    private BigDecimal revenuePreviousPeriod = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal averageOrderValueMonth = BigDecimal.ZERO;

    /** Đơn đang chờ nhân viên xử lý: chờ xác nhận. */
    private long pendingOrders;
    /** Đơn đang trong quá trình: chờ xác nhận, đã xác nhận, chờ nhận hoặc đang giao. */
    private long openOrders;
    /** Số đơn theo trạng thái (nhãn tiếng Việt), mọi thời điểm. */
    @Builder.Default
    private Map<String, Long> ordersByStatus = new LinkedHashMap<>();

    /** Tăng trưởng doanh thu so với cùng kỳ tháng trước, theo phần trăm; null nếu tháng trước chưa có doanh thu để so sánh. */
    public Double getGrowthPercent() {
        if (revenuePreviousPeriod == null || revenuePreviousPeriod.signum() == 0) {
            return null;
        }
        return revenueMonth.subtract(revenuePreviousPeriod).multiply(BigDecimal.valueOf(100))
                .divide(revenuePreviousPeriod, 1, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}
