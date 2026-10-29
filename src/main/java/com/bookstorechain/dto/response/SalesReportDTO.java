package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Doanh thu bán hàng online trong một khoảng ngày (đơn hoàn tất, tính theo ngày hoàn tất). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    /** Chi nhánh được lọc; null là toàn chuỗi. */
    private Long storeId;

    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;
    private long orderCount;
    private long booksSold;
    @Builder.Default
    private BigDecimal discountTotal = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal shippingTotal = BigDecimal.ZERO;

    /** Từng ngày từ startDate đến endDate, ngày không bán được vẫn có dòng với doanh thu 0. */
    @Builder.Default
    private List<DailyRevenue> daily = new ArrayList<>();
    /** Doanh thu theo chi nhánh, nhiều nhất trước. */
    @Builder.Default
    private Map<String, BigDecimal> revenueByStore = new LinkedHashMap<>();
    /** Doanh thu theo hình thức thanh toán (nhãn tiếng Việt). */
    @Builder.Default
    private Map<String, BigDecimal> revenueByPayment = new LinkedHashMap<>();
    @Builder.Default
    private List<TopBook> topBooks = new ArrayList<>();

    public BigDecimal getAverageOrderValue() {
        return orderCount == 0 ? BigDecimal.ZERO : revenue.divide(BigDecimal.valueOf(orderCount), 0, RoundingMode.HALF_UP);
    }

    /** Không có đơn hoàn tất nào trong kỳ. Không đặt tên 'empty': đó là từ khoá của EL, JSP không đọc được ${x.empty}. */
    public boolean isWithoutOrders() {
        return orderCount == 0;
    }

    @Getter
    @AllArgsConstructor
    public static class DailyRevenue {
        private final LocalDate date;
        private final BigDecimal revenue;
        private final long orders;
    }

    @Getter
    @AllArgsConstructor
    public static class TopBook {
        private final Long bookId;
        private final String title;
        private final long copies;
        private final BigDecimal revenue;
    }
}
