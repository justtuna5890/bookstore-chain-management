package com.bookstorechain.service;

import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

public interface PdfExportService {

    /** Xuất phiếu chuyển kho ra PDF */
    void exportTransferDetail(Long transferId, HttpServletResponse response) throws Exception;

    /** Xuất phiếu thu mua ra PDF */
    void exportBuybackDetail(Long buybackId, HttpServletResponse response) throws Exception;

    /** Xuất báo cáo tổng kết vận hành định kỳ khổ A4 */
    void exportPeriodicReportPdf(LocalDate startDate, LocalDate endDate, Long storeId, HttpServletResponse response) throws Exception;
}
