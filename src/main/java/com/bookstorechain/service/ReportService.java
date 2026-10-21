package com.bookstorechain.service;

import com.bookstorechain.dto.response.ReportSummaryDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

public interface ReportService {

    /** Tổng hợp số liệu báo cáo theo khoảng thời gian và chi nhánh */
    ReportSummaryDTO generateReport(LocalDate startDate, LocalDate endDate, Long storeId);

    /** Xuất file Excel báo cáo chi tiết */
    void exportReportExcel(LocalDate startDate, LocalDate endDate, Long storeId, HttpServletResponse response) throws Exception;
}
