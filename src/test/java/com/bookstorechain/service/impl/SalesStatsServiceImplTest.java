package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.SalesOverviewDTO;
import com.bookstorechain.dto.response.SalesReportDTO;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesStatsServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-23T10:00:00Z"), ZoneId.of("UTC"));

    @Mock private CustomerOrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;

    private SalesStatsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalesStatsServiceImpl(orderRepository, orderItemRepository, CLOCK);
        // Mặc định mọi khoảng đều chưa bán được gì; từng test ghi đè khoảng mình quan tâm
        lenient().when(orderRepository.completedTotals(any(), any(), any())).thenReturn(totals("0", 0, "0", "0"));
        lenient().when(orderRepository.countByStatus(any())).thenReturn(List.of());
        lenient().when(orderRepository.dailyRevenue(any(), any(), any())).thenReturn(List.of());
        lenient().when(orderRepository.revenueByStore(any(), any(), any())).thenReturn(List.of());
        lenient().when(orderRepository.revenueByPaymentMethod(any(), any(), any())).thenReturn(List.of());
        lenient().when(orderItemRepository.topSellingBooks(any(), any(), any(), any(Pageable.class))).thenReturn(List.of());
        lenient().when(orderItemRepository.countSoldItems(any(), any(), any())).thenReturn(0L);
    }

    private static List<Object[]> totals(String revenue, long orders, String discount, String shipping) {
        return Collections.singletonList(new Object[]{new BigDecimal(revenue), orders, new BigDecimal(discount), new BigDecimal(shipping)});
    }

    private static LocalDateTime at(int month, int day) {
        return LocalDate.of(2026, month, day).atStartOfDay();
    }

    @Test
    @DisplayName("Tổng quan: doanh thu hôm nay, tháng này và cùng kỳ tháng trước lấy đúng khoảng ngày")
    void overviewUsesTheRightPeriods() {
        when(orderRepository.completedTotals(at(10, 23), at(10, 24), null)).thenReturn(totals("300000", 2, "0", "0"));
        when(orderRepository.completedTotals(at(10, 1), at(10, 24), null)).thenReturn(totals("1500000", 10, "0", "0"));
        // Hôm nay là ngày 23: cùng kỳ tháng trước là 1/9 đến hết 23/9, tức là trước 24/9
        when(orderRepository.completedTotals(at(9, 1), at(9, 24), null)).thenReturn(totals("1000000", 8, "0", "0"));

        SalesOverviewDTO overview = service.getOverview(null);

        assertThat(overview.getRevenueToday()).isEqualByComparingTo("300000");
        assertThat(overview.getOrdersToday()).isEqualTo(2);
        assertThat(overview.getRevenueMonth()).isEqualByComparingTo("1500000");
        assertThat(overview.getOrdersMonth()).isEqualTo(10);
        assertThat(overview.getRevenuePreviousPeriod()).isEqualByComparingTo("1000000");
        assertThat(overview.getAverageOrderValueMonth()).isEqualByComparingTo("150000");
        assertThat(overview.getGrowthPercent()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Tăng trưởng không tính được (null) khi cùng kỳ tháng trước chưa có doanh thu; âm khi giảm")
    void growthEdgeCases() {
        when(orderRepository.completedTotals(at(10, 1), at(10, 24), null)).thenReturn(totals("500000", 3, "0", "0"));
        assertThat(service.getOverview(null).getGrowthPercent()).isNull();

        when(orderRepository.completedTotals(at(9, 1), at(9, 24), null)).thenReturn(totals("1000000", 5, "0", "0"));
        assertThat(service.getOverview(null).getGrowthPercent()).isEqualTo(-50.0);
    }

    @Test
    @DisplayName("Tổng quan trạng thái đơn: đủ mọi trạng thái (thiếu thì 0), đơn chờ và đơn đang xử lý tính đúng")
    void overviewCountsOrdersByStatus() {
        when(orderRepository.countByStatus(null)).thenReturn(List.of(
                new Object[]{OrderStatus.PENDING, 2L}, new Object[]{OrderStatus.CONFIRMED, 1L},
                new Object[]{OrderStatus.SHIPPING, 4L}, new Object[]{OrderStatus.COMPLETED, 30L}, new Object[]{OrderStatus.CANCELLED, 3L}));

        SalesOverviewDTO overview = service.getOverview(null);

        assertThat(overview.getPendingOrders()).isEqualTo(2);
        assertThat(overview.getOpenOrders()).as("chờ xác nhận + đã xác nhận + đang giao").isEqualTo(7);
        assertThat(overview.getOrdersByStatus()).containsEntry("Chờ xác nhận", 2L).containsEntry("Sẵn sàng nhận hàng", 0L)
                .containsEntry("Hoàn tất", 30L).hasSize(OrderStatus.values().length);
        assertThat(overview.getOrdersByStatus().keySet()).first().isEqualTo("Chờ xác nhận");
    }

    @Test
    @DisplayName("Nhân viên chi nhánh: mọi truy vấn đều bị giới hạn bằng storeId được truyền vào")
    void storeScopeIsPassedToEveryQuery() {
        service.getOverview(3L);
        service.getReport(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), 3L);

        verify(orderRepository, org.mockito.Mockito.atLeastOnce()).completedTotals(any(), any(), eq(3L));
        verify(orderRepository).countByStatus(3L);
        verify(orderRepository).dailyRevenue(any(), any(), eq(3L));
        verify(orderRepository).revenueByStore(any(), any(), eq(3L));
        verify(orderRepository).revenueByPaymentMethod(any(), any(), eq(3L));
        verify(orderItemRepository).topSellingBooks(any(), any(), eq(3L), any(Pageable.class));
        verify(orderItemRepository).countSoldItems(any(), any(), eq(3L));
    }

    @Test
    @DisplayName("Báo cáo: dòng cho từng ngày kể cả ngày không bán được, khoảng thời gian gồm cả hai đầu")
    void reportFillsEveryDay() {
        when(orderRepository.completedTotals(at(10, 1), at(10, 4), null)).thenReturn(totals("700000", 3, "20000", "50000"));
        when(orderRepository.dailyRevenue(at(10, 1), at(10, 4), null)).thenReturn(List.of(
                new Object[]{LocalDate.of(2026, 10, 1), new BigDecimal("400000"), 2L},
                new Object[]{LocalDate.of(2026, 10, 3), new BigDecimal("300000"), 1L}));
        when(orderItemRepository.countSoldItems(at(10, 1), at(10, 4), null)).thenReturn(4L);

        SalesReportDTO report = service.getReport(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), null);

        assertThat(report.getDaily()).extracting(SalesReportDTO.DailyRevenue::getDate)
                .containsExactly(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 3));
        assertThat(report.getDaily()).extracting(d -> d.getRevenue().intValue()).containsExactly(400000, 0, 300000);
        assertThat(report.getDaily()).extracting(SalesReportDTO.DailyRevenue::getOrders).containsExactly(2L, 0L, 1L);
        assertThat(report.getRevenue()).isEqualByComparingTo("700000");
        assertThat(report.getOrderCount()).isEqualTo(3);
        assertThat(report.getBooksSold()).isEqualTo(4);
        assertThat(report.getDiscountTotal()).isEqualByComparingTo("20000");
        assertThat(report.getShippingTotal()).isEqualByComparingTo("50000");
        assertThat(report.getAverageOrderValue()).isEqualByComparingTo("233333");
        assertThat(report.isWithoutOrders()).isFalse();
    }

    @Test
    @DisplayName("Báo cáo: doanh thu theo chi nhánh, theo hình thức thanh toán (nhãn tiếng Việt) và sách bán chạy")
    void reportBreakdowns() {
        when(orderRepository.revenueByStore(any(), any(), any())).thenReturn(List.of(
                new Object[]{"OldBook Ha Noi", new BigDecimal("900000")}, new Object[]{"OldBook Sai Gon", new BigDecimal("400000")}));
        when(orderRepository.revenueByPaymentMethod(any(), any(), any())).thenReturn(List.of(
                new Object[]{PaymentMethod.COD, new BigDecimal("500000")}, new Object[]{PaymentMethod.BANK_TRANSFER, new BigDecimal("800000")}));
        when(orderItemRepository.topSellingBooks(any(), any(), any(), any(Pageable.class))).thenReturn(List.of(
                new Object[]{7L, "Sapiens", 5L, new BigDecimal("640000")}, new Object[]{2L, "Nha Gia Kim", 3L, new BigDecimal("250000")}));

        SalesReportDTO report = service.getReport(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), null);

        assertThat(report.getRevenueByStore().keySet()).containsExactly("OldBook Ha Noi", "OldBook Sai Gon");
        assertThat(report.getRevenueByPayment()).containsKeys("Tiền mặt khi nhận hàng", "Chuyển khoản");
        assertThat(report.getTopBooks()).extracting(SalesReportDTO.TopBook::getTitle).containsExactly("Sapiens", "Nha Gia Kim");
        assertThat(report.getTopBooks().get(0).getCopies()).isEqualTo(5);
        assertThat(report.getTopBooks().get(0).getRevenue()).isEqualByComparingTo("640000");
    }

    @Test
    @DisplayName("Báo cáo yêu cầu top sách bán chạy giới hạn 10 dòng")
    void topBooksAreLimited() {
        service.getReport(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(orderItemRepository).topSellingBooks(any(), any(), any(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Hai đầu ngày bị đảo thì tự đảo lại; thiếu ngày thì lấy 30 ngày gần nhất tính tới hôm nay")
    void normalisesTheRange() {
        SalesReportDTO swapped = service.getReport(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 1), null);
        assertThat(swapped.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(swapped.getEndDate()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(swapped.getDaily()).hasSize(5);

        SalesReportDTO defaults = service.getReport(null, null, null);
        assertThat(defaults.getEndDate()).isEqualTo(LocalDate.of(2026, 10, 23));
        assertThat(defaults.getStartDate()).isEqualTo(LocalDate.of(2026, 9, 24));
        assertThat(defaults.getDaily()).hasSize(30);
        assertThat(defaults.isWithoutOrders()).isTrue();
        assertThat(defaults.getAverageOrderValue()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Khoảng thời gian dài hơn 366 ngày bị từ chối, đúng 366 ngày thì được")
    void rejectsTooLongRange() {
        LocalDate end = LocalDate.of(2026, 10, 23);

        assertThat(service.getReport(end.minusDays(365), end, null).getDaily()).hasSize(366);
        assertThatThrownBy(() -> service.getReport(end.minusDays(366), end, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("366");
    }
}
