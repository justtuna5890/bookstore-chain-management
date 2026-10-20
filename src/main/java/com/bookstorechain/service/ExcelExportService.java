package com.bookstorechain.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ExcelExportService {

    /** Xuất danh sách tồn kho (BookCopy) ra file .xlsx */
    void exportInventory(Long storeId, HttpServletResponse response) throws Exception;

    /** Xuất danh sách phiếu chuyển kho ra file .xlsx */
    void exportTransfers(HttpServletResponse response) throws Exception;

    /** Xuất danh sách yêu cầu thu mua ra file .xlsx */
    void exportBuybacks(HttpServletResponse response) throws Exception;
}
