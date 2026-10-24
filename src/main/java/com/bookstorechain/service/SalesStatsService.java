package com.bookstorechain.service;

import com.bookstorechain.dto.response.SalesOverviewDTO;
import com.bookstorechain.dto.response.SalesReportDTO;

import java.time.LocalDate;

/**
 * Thống kê doanh thu bán hàng online cho dashboard và báo cáo. Chỉ đơn hoàn tất được tính, theo ngày hoàn tất;
 * doanh thu là tổng thanh toán của đơn (đã trừ giảm giá, đã gồm phí giao hàng).
 * storeId null là toàn chuỗi (quản lý, quản trị); nhân viên phải truyền chi nhánh của mình.
 */
public interface SalesStatsService {

    SalesOverviewDTO getOverview(Long storeId);

    /** Báo cáo từ startDate đến endDate (gồm cả hai đầu). Hai đầu đảo ngược thì tự đảo lại; tối đa 366 ngày. */
    SalesReportDTO getReport(LocalDate startDate, LocalDate endDate, Long storeId);
}
