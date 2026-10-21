package com.bookstorechain.controller.web;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.enums.Role;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.BuybackService;
import com.bookstorechain.service.ExcelExportService;
import com.bookstorechain.service.PdfExportService;
import com.bookstorechain.service.StockTransferService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final StockTransferService stockTransferService;
    private final BuybackService buybackService;

    // ==================== EXCEL ====================

    /** Xuất tồn kho ra Excel (giới hạn nghiêm ngặt theo chi nhánh của STAFF) */
    @GetMapping("/excel/inventory")
    public void exportInventoryExcel(@RequestParam(value = "storeId", required = false) Long storeId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     HttpServletResponse response) throws Exception {
        if (userDetails != null && userDetails.getUser().getRole() == Role.STAFF) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản nhân viên chưa được gán chi nhánh để xuất báo cáo tồn kho!");
                return;
            }
            storeId = staffStoreId;
        }
        excelExportService.exportInventory(storeId, response);
    }

    /** Xuất danh sách phiếu chuyển kho ra Excel (STAFF chỉ xuất phiếu liên quan chi nhánh mình) */
    @GetMapping("/excel/transfers")
    public void exportTransfersExcel(@RequestParam(value = "storeId", required = false) Long storeId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     HttpServletResponse response) throws Exception {
        if (userDetails != null && userDetails.getUser().getRole() == Role.STAFF) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản nhân viên chưa được gán chi nhánh để xuất phiếu chuyển kho!");
                return;
            }
            storeId = staffStoreId;
        }
        excelExportService.exportTransfers(storeId, response);
    }

    /** Xuất danh sách đơn thu mua ra Excel (STAFF chỉ xuất đơn chi nhánh mình phụ trách) */
    @GetMapping("/excel/buybacks")
    public void exportBuybacksExcel(@RequestParam(value = "storeId", required = false) Long storeId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    HttpServletResponse response) throws Exception {
        if (userDetails != null && userDetails.getUser().getRole() == Role.STAFF) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản nhân viên chưa được gán chi nhánh để xuất đơn thu mua!");
                return;
            }
            storeId = staffStoreId;
        }
        excelExportService.exportBuybacks(storeId, response);
    }

    // ==================== PDF ====================

    /** Xuất chi tiết phiếu chuyển kho ra PDF */
    @GetMapping("/pdf/transfer/{id}")
    public void exportTransferPdf(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  HttpServletResponse response) throws Exception {
        if (userDetails != null && userDetails.getUser().getRole() == Role.STAFF) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            StockTransfer transfer = stockTransferService.getTransferById(id);
            boolean isRelated = staffStoreId != null &&
                    (staffStoreId.equals(transfer.getSenderStore().getId()) || staffStoreId.equals(transfer.getReceiverStore().getId()));
            if (!isRelated) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Nhân viên chỉ có quyền xuất PDF phiếu chuyển kho của chi nhánh mình!");
                return;
            }
        }
        pdfExportService.exportTransferDetail(id, response);
    }

    /** Xuất chi tiết đơn thu mua ra PDF */
    @GetMapping("/pdf/buyback/{id}")
    public void exportBuybackPdf(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 HttpServletResponse response) throws Exception {
        if (userDetails != null && userDetails.getUser().getRole() == Role.STAFF) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            BuybackRequest request = buybackService.getRequestById(id);
            if (staffStoreId == null || !staffStoreId.equals(request.getStore().getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Nhân viên chỉ có quyền xuất PDF đơn thu mua của chi nhánh mình!");
                return;
            }
        }
        pdfExportService.exportBuybackDetail(id, response);
    }
}
