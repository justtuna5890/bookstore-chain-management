package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.ReportSummaryDTO;
import com.bookstorechain.service.ReportService;
import com.bookstorechain.service.StoreService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;
    private final StoreService storeService;

    @GetMapping
    public String showReports(@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(value = "storeId", required = false) Long storeId,
                              @RequestParam(value = "tab", defaultValue = "overview") String tab,
                              Model model) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        ReportSummaryDTO summary = reportService.generateReport(startDate, endDate, storeId);

        model.addAttribute("summary", summary);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("currentTab", tab);
        model.addAttribute("stores", storeService.getAllStores());

        return "admin/report/report-dashboard";
    }

    @GetMapping("/export/excel")
    public void exportReportExcel(@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                  @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                  @RequestParam(value = "storeId", required = false) Long storeId,
                                  HttpServletResponse response) throws Exception {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        reportService.exportReportExcel(startDate, endDate, storeId, response);
    }
}
