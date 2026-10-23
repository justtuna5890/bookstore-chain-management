package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.SalesOverviewDTO;
import com.bookstorechain.dto.response.SalesReportDTO;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.OrderItemRepository;
import com.bookstorechain.service.SalesStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesStatsServiceImpl implements SalesStatsService {

    static final int MAX_RANGE_DAYS = 366;
    static final int TOP_BOOKS_LIMIT = 10;
    private static final int DEFAULT_RANGE_DAYS = 30;

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public SalesOverviewDTO getOverview(Long storeId) {
        LocalDate today = LocalDate.now(clock);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate tomorrow = today.plusDays(1);

        BigDecimal[] day = totals(today, tomorrow, storeId);
        BigDecimal[] month = totals(monthStart, tomorrow, storeId);
        // Cùng kỳ tháng trước: từ ngày 1 đến đúng số ngày đã trôi qua của tháng này, không vượt quá tháng trước
        LocalDate previousStart = monthStart.minusMonths(1);
        LocalDate previousEnd = previousStart.plusDays(Math.min(today.getDayOfMonth(), previousStart.lengthOfMonth()));
        BigDecimal[] previous = totals(previousStart, previousEnd, storeId);

        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (Object[] row : orderRepository.countByStatus(storeId)) {
            counts.put((OrderStatus) row[0], ((Number) row[1]).longValue());
        }
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status.getLabel(), counts.getOrDefault(status, 0L));
        }
        long pending = counts.getOrDefault(OrderStatus.PENDING, 0L);
        long open = pending + counts.getOrDefault(OrderStatus.CONFIRMED, 0L)
                + counts.getOrDefault(OrderStatus.READY_FOR_PICKUP, 0L) + counts.getOrDefault(OrderStatus.SHIPPING, 0L);

        long monthOrders = month[1].longValue();
        return SalesOverviewDTO.builder()
                .revenueToday(day[0]).ordersToday(day[1].longValue())
                .revenueMonth(month[0]).ordersMonth(monthOrders)
                .revenuePreviousPeriod(previous[0])
                .averageOrderValueMonth(monthOrders == 0 ? BigDecimal.ZERO
                        : month[0].divide(BigDecimal.valueOf(monthOrders), 0, java.math.RoundingMode.HALF_UP))
                .pendingOrders(pending).openOrders(open).ordersByStatus(byStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getReport(LocalDate startDate, LocalDate endDate, Long storeId) {
        LocalDate end = endDate != null ? endDate : LocalDate.now(clock);
        LocalDate start = startDate != null ? startDate : end.minusDays(DEFAULT_RANGE_DAYS - 1L);
        if (start.isAfter(end)) {
            LocalDate swap = start;
            start = end;
            end = swap;
        }
        if (ChronoUnit.DAYS.between(start, end) + 1 > MAX_RANGE_DAYS) {
            throw new BusinessException("Báo cáo doanh thu chỉ hỗ trợ khoảng thời gian tối đa " + MAX_RANGE_DAYS + " ngày.");
        }
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.plusDays(1).atStartOfDay();

        BigDecimal[] totals = totals(start, end.plusDays(1), storeId);

        Map<LocalDate, Object[]> byDay = new LinkedHashMap<>();
        for (Object[] row : orderRepository.dailyRevenue(from, to, storeId)) {
            byDay.put((LocalDate) row[0], row);
        }
        List<SalesReportDTO.DailyRevenue> daily = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Object[] row = byDay.get(date);
            daily.add(new SalesReportDTO.DailyRevenue(date,
                    row == null ? BigDecimal.ZERO : money(row[1]), row == null ? 0 : ((Number) row[2]).longValue()));
        }

        Map<String, BigDecimal> byStore = new LinkedHashMap<>();
        for (Object[] row : orderRepository.revenueByStore(from, to, storeId)) {
            byStore.put((String) row[0], money(row[1]));
        }
        Map<String, BigDecimal> byPayment = new LinkedHashMap<>();
        for (Object[] row : orderRepository.revenueByPaymentMethod(from, to, storeId)) {
            byPayment.put(paymentLabel((PaymentMethod) row[0]), money(row[1]));
        }
        List<SalesReportDTO.TopBook> topBooks = new ArrayList<>();
        for (Object[] row : orderItemRepository.topSellingBooks(from, to, storeId, PageRequest.of(0, TOP_BOOKS_LIMIT))) {
            topBooks.add(new SalesReportDTO.TopBook(((Number) row[0]).longValue(), (String) row[1],
                    ((Number) row[2]).longValue(), money(row[3])));
        }

        return SalesReportDTO.builder()
                .startDate(start).endDate(end).storeId(storeId)
                .revenue(totals[0]).orderCount(totals[1].longValue())
                .discountTotal(totals[2]).shippingTotal(totals[3])
                .booksSold(orderItemRepository.countSoldItems(from, to, storeId))
                .daily(daily).revenueByStore(byStore).revenueByPayment(byPayment).topBooks(topBooks)
                .build();
    }

    /** [doanh thu, số đơn, tổng giảm giá, phí giao hàng] của các đơn hoàn tất từ ngày from đến trước ngày to. */
    private BigDecimal[] totals(LocalDate from, LocalDate to, Long storeId) {
        Object[] row = orderRepository.completedTotals(from.atStartOfDay(), to.atStartOfDay(), storeId).get(0);
        return new BigDecimal[]{money(row[0]), money(row[1]), money(row[2]), money(row[3])};
    }

    private BigDecimal money(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
    }

    private String paymentLabel(PaymentMethod method) {
        return method == PaymentMethod.COD ? "Tiền mặt khi nhận hàng" : "Chuyển khoản";
    }
}
