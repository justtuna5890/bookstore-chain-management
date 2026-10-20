package com.bookstorechain.controller.web;

import com.bookstorechain.service.ExcelExportService;
import com.bookstorechain.service.PdfExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/export")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class ExportController {

    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    // ==================== EXCEL ====================

    /** Xuất tồn kho ra Excel (có thể lọc theo chi nhánh) */
    @GetMapping("/excel/inventory")
    public void exportInventoryExcel(@RequestParam(value = "storeId", required = false) Long storeId,
                                     @org.springframework.security.core.annotation.AuthenticationPrincipal com.bookstorechain.security.CustomUserDetails userDetails,
                                     HttpServletResponse response) throws Exception {
        // Nếu là STAFF, chỉ cho phép xuất dữ liệu chi nhánh mình
        if (userDetails != null && userDetails.getUser().getRole() == com.bookstorechain.enums.Role.STAFF) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            storeId = staffStoreId;
        }
        excelExportService.exportInventory(storeId, response);
    }

    /** Xuất danh sách phiếu chuyển kho ra Excel */
    @GetMapping("/excel/transfers")
    public void exportTransfersExcel(HttpServletResponse response) throws Exception {
        excelExportService.exportTransfers(response);
    }

    /** Xuất danh sách đơn thu mua ra Excel */
    @GetMapping("/excel/buybacks")
    public void exportBuybacksExcel(HttpServletResponse response) throws Exception {
        excelExportService.exportBuybacks(response);
    }

    // ==================== PDF ====================

    /** Xuất chi tiết phiếu chuyển kho ra PDF */
    @GetMapping("/pdf/transfer/{id}")
    public void exportTransferPdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        pdfExportService.exportTransferDetail(id, response);
    }

    /** Xuất chi tiết đơn thu mua ra PDF */
    @GetMapping("/pdf/buyback/{id}")
    public void exportBuybackPdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        pdfExportService.exportBuybackDetail(id, response);
    }
}
