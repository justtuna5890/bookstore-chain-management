package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.OperationalAlertDTO;
import com.bookstorechain.dto.response.SalesReportDTO;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.enums.Role;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.DashboardService;
import com.bookstorechain.service.SalesStatsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final SalesStatsService salesStatsService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Role userRole = userDetails != null && userDetails.getUser() != null ? userDetails.getUser().getRole() : Role.STAFF;
        boolean isAdmin = userRole == Role.ADMIN;
        boolean isManager = userRole == Role.MANAGER;
        boolean isStaff = userRole == Role.STAFF;
        boolean isScoped = !isAdmin; // Quản lý (MANAGER) và Nhân viên (STAFF) chỉ được quản lý chi nhánh cụ thể

        model.addAttribute("userRole", userRole.name());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);
        model.addAttribute("isStaff", isStaff);

        Long scopedStoreId = isScoped && userDetails != null && userDetails.getUser() != null && userDetails.getUser().getStore() != null
                ? userDetails.getUser().getStore().getId() : null;

        Map<String, Long> stats;
        BigDecimal totalInventoryValue = BigDecimal.ZERO;
        Map<String, BigDecimal> storeValues = dashboardService.getInventoryValueByStore();

        if (isScoped) {
            if (scopedStoreId == null) {
                // Nhân viên hoặc Quản lý chưa được gán chi nhánh: KHÔNG cấp quyền xem toàn bộ kho
                model.addAttribute("unassignedStoreWarning", true);
                model.addAttribute("isScopedToStore", true);
                model.addAttribute("currentStoreName", "Chưa phân bổ chi nhánh");
                model.addAttribute("currentStoreId", null);

                stats = Map.of(
                        "totalBooks", 0L,
                        "totalStores", 0L,
                        "totalBookCopies", 0L,
                        "totalBuybacks", 0L,
                        "totalTransfers", 0L
                );
                model.addAttribute("stats", stats);
                model.addAttribute("stores", List.of());
                setDefaultChartData(model);
            } else {
                // Quản lý hoặc Nhân viên: Giới hạn theo đúng chi nhánh phụ trách
                String storeName = userDetails.getUser().getStore().getName();
                model.addAttribute("isScopedToStore", true);
                model.addAttribute("currentStoreName", storeName);
                model.addAttribute("currentStoreId", scopedStoreId);

                stats = dashboardService.getDashboardStatisticsByStore(scopedStoreId);
                model.addAttribute("stats", stats);
                model.addAttribute("stores", List.of(userDetails.getUser().getStore()));

                totalInventoryValue = storeValues.getOrDefault(storeName, BigDecimal.ZERO);

                try {
                    Map<String, Long> staffInv = Map.of(storeName, stats.getOrDefault("totalBookCopies", 0L));
                    Map<String, BigDecimal> staffVal = Map.of(storeName, totalInventoryValue);

                    model.addAttribute("inventoryByStoreJson", objectMapper.writeValueAsString(staffInv));
                    model.addAttribute("inventoryValueJson", objectMapper.writeValueAsString(staffVal));
                    model.addAttribute("conditionBreakdownJson", objectMapper.writeValueAsString(dashboardService.getConditionBreakdownByStore(scopedStoreId)));
                    model.addAttribute("buybackStatusJson", objectMapper.writeValueAsString(dashboardService.getBuybackStatusBreakdownByStore(scopedStoreId)));
                } catch (JsonProcessingException e) {
                    log.error("Lỗi chuyển đổi dữ liệu thống kê JSON cho chi nhánh", e);
                    setDefaultChartData(model);
                }
            }
        } else {
            // ADMIN: Xem toàn hệ thống chuỗi
            model.addAttribute("isScopedToStore", false);
            if (userDetails != null && userDetails.getUser().getStore() != null) {
                model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
                model.addAttribute("currentStoreId", userDetails.getUser().getStore().getId());
            }

            stats = dashboardService.getDashboardStatistics();
            model.addAttribute("stats", stats);
            model.addAttribute("stores", dashboardService.getAllStores());

            for (BigDecimal val : storeValues.values()) {
                if (val != null) {
                    totalInventoryValue = totalInventoryValue.add(val);
                }
            }

            try {
                model.addAttribute("inventoryByStoreJson", objectMapper.writeValueAsString(dashboardService.getInventoryByStore()));
                model.addAttribute("inventoryValueJson", objectMapper.writeValueAsString(storeValues));
                model.addAttribute("conditionBreakdownJson", objectMapper.writeValueAsString(dashboardService.getConditionBreakdown()));
                model.addAttribute("buybackStatusJson", objectMapper.writeValueAsString(dashboardService.getBuybackStatusBreakdown()));
            } catch (JsonProcessingException e) {
                log.error("Lỗi chuyển đổi dữ liệu thống kê JSON", e);
                setDefaultChartData(model);
            }
        }

        // Bổ sung các biến thống kê trực tiếp để hiển thị trên KPI cards
        model.addAttribute("totalStores", stats.getOrDefault("totalStores", 0L));
        model.addAttribute("totalBooks", stats.getOrDefault("totalBooks", 0L));
        model.addAttribute("availableCopies", stats.getOrDefault("totalBookCopies", 0L));
        model.addAttribute("totalBuybacks", stats.getOrDefault("totalBuybacks", 0L));
        model.addAttribute("totalTransfers", stats.getOrDefault("totalTransfers", 0L));
        model.addAttribute("totalInventoryValue", totalInventoryValue);

        // Danh sách yêu cầu thu mua & phiếu điều chuyển gần đây từ Service
        List<BuybackRequest> recentBuybacks;
        List<StockTransfer> recentTransfers;
        if (isScoped) {
            if (scopedStoreId == null) {
                recentBuybacks = List.of();
                recentTransfers = List.of();
            } else {
                recentBuybacks = dashboardService.getRecentBuybacks(scopedStoreId, 5);
                recentTransfers = dashboardService.getRecentTransfers(scopedStoreId, 5);
            }
        } else {
            recentBuybacks = dashboardService.getRecentBuybacks(null, 5);
            recentTransfers = dashboardService.getRecentTransfers(null, 5);
        }
        model.addAttribute("recentBuybacks", recentBuybacks);
        model.addAttribute("recentTransfers", recentTransfers);

        // Trung tâm cảnh báo vận hành & Chỉ số mở rộng
        Long targetStoreId = isScoped ? scopedStoreId : null;
        List<OperationalAlertDTO> alerts;
        Map<String, Object> extendedMetrics;
        if (isScoped && scopedStoreId == null) {
            alerts = List.of();
            extendedMetrics = Map.of(
                    "pendingBuybacks", 0L,
                    "activeTransfers", 0L,
                    "lowStockCount", 0L,
                    "buybackApprovalRate", 0.0,
                    "staleCopiesCount", 0L
            );
        } else {
            alerts = dashboardService.getOperationalAlerts(targetStoreId);
            extendedMetrics = dashboardService.getExtendedMetrics(targetStoreId);
        }
        model.addAttribute("operationalAlerts", alerts);
        model.addAttribute("extendedMetrics", extendedMetrics);

        // Ma trận luân chuyển
        Map<String, Long> transferFlow = dashboardService.getTransferFlowMatrix();
        model.addAttribute("transferFlow", transferFlow);
        try {
            model.addAttribute("transferFlowJson", objectMapper.writeValueAsString(transferFlow));
        } catch (JsonProcessingException e) {
            model.addAttribute("transferFlowJson", "{}");
        }

        addSalesSection(model, isScoped, scopedStoreId, userDetails);

        return "admin/dashboard";
    }

    /**
     * Khu vực doanh thu bán hàng online. Quản lý và nhân viên chỉ thấy doanh thu chi nhánh mình;
     * chỉ quản trị (ADMIN) mới thấy toàn chuỗi cùng doanh thu theo chi nhánh.
     */
    private void addSalesSection(Model model, boolean isScoped, Long scopedStoreId, CustomUserDetails userDetails) {
        if (isScoped && scopedStoreId == null) {
            return;
        }
        Long scope = isScoped ? scopedStoreId : null;
        SalesReportDTO report = salesStatsService.getReport(null, null, scope);
        model.addAttribute("salesOverview", salesStatsService.getOverview(scope));
        model.addAttribute("salesReport", report);
        model.addAttribute("salesScopeLabel", isScoped ? (userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getName() : "Chi nhánh phụ trách") : "Toàn chuỗi");
        model.addAttribute("salesShowStores", !isScoped);
        model.addAttribute("salesDailyJson", ScriptJson.of(objectMapper, report.getDaily(), "[]"));
        model.addAttribute("salesPaymentJson", ScriptJson.of(objectMapper, report.getRevenueByPayment(), "{}"));
        model.addAttribute("salesStoreJson", ScriptJson.of(objectMapper, report.getRevenueByStore(), "{}"));
    }

    private void setDefaultChartData(Model model) {
        model.addAttribute("inventoryByStoreJson", "{}");
        model.addAttribute("inventoryValueJson", "{}");
        model.addAttribute("conditionBreakdownJson", "{}");
        model.addAttribute("buybackStatusJson", "{}");
        model.addAttribute("transferFlowJson", "{}");
    }
}
