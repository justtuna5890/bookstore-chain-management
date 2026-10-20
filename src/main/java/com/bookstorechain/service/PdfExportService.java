package com.bookstorechain.service;

import jakarta.servlet.http.HttpServletResponse;

public interface PdfExportService {

    /** Xuất phiếu chuyển kho ra PDF */
    void exportTransferDetail(Long transferId, HttpServletResponse response) throws Exception;

    /** Xuất phiếu thu mua ra PDF */
    void exportBuybackDetail(Long buybackId, HttpServletResponse response) throws Exception;
}
