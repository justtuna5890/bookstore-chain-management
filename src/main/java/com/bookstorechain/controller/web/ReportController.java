package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.ReportSummaryDTO;
import com.bookstorechain.enums.Role;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.PdfExportService;
import com.bookstorechain.service.ReportService;
import com.bookstorechain.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class ReportController {

    private final ReportService reportService;
    private final StoreService storeService;
    private final PdfExportService pdfExportService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String showReports(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(value = "storeId", required = false) Long storeId,
                              @RequestParam(value = "tab", defaultValue = "overview") String tab,
                              Model model) {
        Role userRole = userDetails != null && userDetails.getUser() != null ? userDetails.getUser().getRole() : Role.STAFF;
        boolean isScoped = userRole == Role.STAFF || userRole == Role.MANAGER;

        // Quản lý và nhân viên bị khóa cứng theo chi nhánh công tác
        if (isScoped) {
            storeId = scopedStoreIdOrDeny(userDetails);
            model.addAttribute("isScopedToStore", true);
        } else {
            model.addAttribute("isScopedToStore", false);
        }

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        ReportSummaryDTO summary = reportService.generateReport(startDate, endDate, storeId);

        model.addAttribute("summary", summary);
        if (summary.getSales() != null) {
            model.addAttribute("salesDailyJson", ScriptJson.of(objectMapper, summary.getSales().getDaily(), "[]"));
            model.addAttribute("salesPaymentJson", ScriptJson.of(objectMapper, summary.getSales().getRevenueByPayment(), "{}"));
            model.addAttribute("salesStoreJson", ScriptJson.of(objectMapper, summary.getSales().getRevenueByStore(), "{}"));
        }
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("currentTab", tab);
        model.addAttribute("stores", isScoped && userDetails.getUser().getStore() != null ? List.of(userDetails.getUser().getStore()) : storeService.getAllStores());
        model.addAttribute("isStaff", isScoped);

        return "admin/report/report-dashboard";
    }

    @GetMapping("/export/excel")
    public void exportReportExcel(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                  @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                  @RequestParam(value = "storeId", required = false) Long storeId,
                                  HttpServletResponse response) throws Exception {
        Role userRole = userDetails != null && userDetails.getUser() != null ? userDetails.getUser().getRole() : Role.STAFF;
        if (userRole == Role.STAFF || userRole == Role.MANAGER) {
            storeId = scopedStoreIdOrDeny(userDetails);
        }

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        reportService.exportReportExcel(startDate, endDate, storeId, response);
    }

    @GetMapping("/export/pdf")
    public void exportReportPdf(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam(value = "storeId", required = false) Long storeId,
                                HttpServletResponse response) throws Exception {
        Role userRole = userDetails != null && userDetails.getUser() != null ? userDetails.getUser().getRole() : Role.STAFF;
        if (userRole == Role.STAFF || userRole == Role.MANAGER) {
            storeId = scopedStoreIdOrDeny(userDetails);
        }

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        pdfExportService.exportPeriodicReportPdf(startDate, endDate, storeId, response);
    }

    /**
     * Chi nhánh mà nhân viên / quản lý được xem báo cáo. Chưa gán chi nhánh bị từ chối: truyền null xuống tầng báo cáo
     * nghĩa là toàn chuỗi, sẽ lộ số liệu và doanh thu của mọi chi nhánh.
     */
    private Long scopedStoreIdOrDeny(CustomUserDetails userDetails) {
        if (userDetails.getUser().getStore() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản chưa được gán chi nhánh nên không xem được báo cáo.");
        }
        return userDetails.getUser().getStore().getId();
    }
}
