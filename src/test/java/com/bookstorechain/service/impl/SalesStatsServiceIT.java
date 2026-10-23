package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.SalesOverviewDTO;
import com.bookstorechain.dto.response.SalesReportDTO;
import com.bookstorechain.service.SalesStatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm chứng các truy vấn thống kê doanh thu trên SQL Server thật bằng cách đối chiếu với SQL thuần trên cùng dữ liệu
 * (các HQL như CAST(... AS LocalDate), nối bảng thanh toán, nhóm theo chi nhánh chỉ chạy được trên database thật).
 * Chỉ đọc dữ liệu nên không dọn gì; kỳ vọng tính từ chính database nên đúng bất kể ngày chạy.
 * Chỉ chạy khi RUN_DB_TESTS=true: mvn test -Dtest=SalesStatsServiceIT
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class SalesStatsServiceIT {

    private static final String DONE_IN_WINDOW = "status = 'COMPLETED' AND completed_at >= ? AND completed_at < ?";

    @Autowired private SalesStatsService salesStatsService;
    @Autowired private JdbcTemplate jdbc;

    private static Timestamp ts(LocalDate date) {
        return Timestamp.valueOf(date.atStartOfDay());
    }

    private BigDecimal sum(String sql, Object... args) {
        BigDecimal value = jdbc.queryForObject(sql, BigDecimal.class, args);
        return value == null ? BigDecimal.ZERO : value;
    }

    @Test
    @DisplayName("Báo cáo 30 ngày khớp SQL thuần: doanh thu, số đơn, giảm giá, phí giao hàng, số cuốn đã bán")
    void reportMatchesRawSql() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        Timestamp from = ts(start);
        Timestamp to = ts(end.plusDays(1));

        SalesReportDTO report = salesStatsService.getReport(start, end, null);

        assertThat(report.getRevenue()).isEqualByComparingTo(sum("SELECT COALESCE(SUM(total_amount), 0) FROM customer_order WHERE " + DONE_IN_WINDOW, from, to));
        assertThat(report.getOrderCount()).isEqualTo(jdbc.queryForObject("SELECT COUNT(*) FROM customer_order WHERE " + DONE_IN_WINDOW, Long.class, from, to));
        assertThat(report.getDiscountTotal()).isEqualByComparingTo(sum("SELECT COALESCE(SUM(discount_amount), 0) FROM customer_order WHERE " + DONE_IN_WINDOW, from, to));
        assertThat(report.getShippingTotal()).isEqualByComparingTo(sum("SELECT COALESCE(SUM(shipping_fee), 0) FROM customer_order WHERE " + DONE_IN_WINDOW, from, to));
        assertThat(report.getBooksSold()).isEqualTo(jdbc.queryForObject(
                "SELECT COUNT(*) FROM order_item i JOIN customer_order o ON o.id = i.order_id WHERE o." + DONE_IN_WINDOW, Long.class, from, to));
        assertThat(report.getOrderCount()).as("dữ liệu mẫu có đơn hoàn tất để thống kê").isPositive();
    }

    @Test
    @DisplayName("Chuỗi theo ngày đủ 30 dòng, tổng các ngày bằng tổng doanh thu; theo chi nhánh và hình thức thanh toán cũng cộng đúng tổng")
    void breakdownsAddUpToTheTotal() {
        LocalDate end = LocalDate.now();
        SalesReportDTO report = salesStatsService.getReport(end.minusDays(29), end, null);

        assertThat(report.getDaily()).hasSize(30);
        BigDecimal dailySum = report.getDaily().stream().map(SalesReportDTO.DailyRevenue::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(dailySum).isEqualByComparingTo(report.getRevenue());
        assertThat(report.getDaily().stream().mapToLong(SalesReportDTO.DailyRevenue::getOrders).sum()).isEqualTo(report.getOrderCount());
        assertThat(report.getRevenueByStore().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo(report.getRevenue());
        assertThat(report.getRevenueByPayment().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo(report.getRevenue());
        assertThat(report.getTopBooks()).isNotEmpty();
        assertThat(report.getTopBooks().stream().mapToLong(SalesReportDTO.TopBook::getCopies).sum()).isLessThanOrEqualTo(report.getBooksSold());
        // Chi nhánh bán nhiều nhất đứng đầu
        Map<String, BigDecimal> byStore = new LinkedHashMap<>(report.getRevenueByStore());
        BigDecimal previous = null;
        for (BigDecimal value : byStore.values()) {
            if (previous != null) {
                assertThat(value).isLessThanOrEqualTo(previous);
            }
            previous = value;
        }
    }

    @Test
    @DisplayName("Lọc theo chi nhánh: mỗi chi nhánh khớp SQL thuần và chỉ còn đúng chi nhánh đó")
    void storeScopeMatchesRawSql() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        Timestamp from = ts(start);
        Timestamp to = ts(end.plusDays(1));

        for (Long storeId : jdbc.queryForList("SELECT id FROM store", Long.class)) {
            SalesReportDTO report = salesStatsService.getReport(start, end, storeId);

            assertThat(report.getRevenue()).as("chi nhánh %d", storeId)
                    .isEqualByComparingTo(sum("SELECT COALESCE(SUM(total_amount), 0) FROM customer_order WHERE store_id = " + storeId + " AND " + DONE_IN_WINDOW, from, to));
            assertThat(report.getRevenueByStore().size()).isLessThanOrEqualTo(1);
        }
    }

    @Test
    @DisplayName("Tổng quan: doanh thu hôm nay và tháng này khớp SQL thuần, số đơn theo trạng thái khớp bảng đơn hàng")
    void overviewMatchesRawSql() {
        LocalDate today = LocalDate.now();
        SalesOverviewDTO overview = salesStatsService.getOverview(null);

        assertThat(overview.getRevenueToday()).isEqualByComparingTo(sum(
                "SELECT COALESCE(SUM(total_amount), 0) FROM customer_order WHERE " + DONE_IN_WINDOW, ts(today), ts(today.plusDays(1))));
        assertThat(overview.getRevenueMonth()).isEqualByComparingTo(sum(
                "SELECT COALESCE(SUM(total_amount), 0) FROM customer_order WHERE " + DONE_IN_WINDOW, ts(today.withDayOfMonth(1)), ts(today.plusDays(1))));
        assertThat(overview.getPendingOrders()).isEqualTo(jdbc.queryForObject("SELECT COUNT(*) FROM customer_order WHERE status = 'PENDING'", Long.class));
        assertThat(overview.getOpenOrders()).isEqualTo(jdbc.queryForObject(
                "SELECT COUNT(*) FROM customer_order WHERE status IN ('PENDING','CONFIRMED','READY_FOR_PICKUP','SHIPPING')", Long.class));
        assertThat(overview.getOrdersByStatus().values().stream().mapToLong(Long::longValue).sum())
                .isEqualTo(jdbc.queryForObject("SELECT COUNT(*) FROM customer_order", Long.class));
    }
}
