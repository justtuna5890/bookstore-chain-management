package com.bookstorechain.controller.web;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.enums.Role;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StockTransferRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.DashboardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final StoreRepository storeRepository;
    private final BuybackRequestRepository buybackRequestRepository;
    private final StockTransferRepository stockTransferRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Role userRole = userDetails != null && userDetails.getUser() != null ? userDetails.getUser().getRole() : Role.STAFF;
        boolean isAdmin = userRole == Role.ADMIN;
        boolean isManager = userRole == Role.MANAGER;
        boolean isStaff = userRole == Role.STAFF;

        model.addAttribute("userRole", userRole.name());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);
        model.addAttribute("isStaff", isStaff);

        Long staffStoreId = isStaff && userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;

        Map<String, Long> stats;
        BigDecimal totalInventoryValue = BigDecimal.ZERO;
        Map<String, BigDecimal> storeValues = dashboardService.getInventoryValueByStore();

        if (isStaff) {
            if (staffStoreId == null) {
                // Nhân viên chưa được gán chi nhánh: KHÔNG cấp quyền xem toàn bộ kho
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
                // Nhân viên STAFF: Giới hạn theo chi nhánh phụ trách
                String storeName = userDetails.getUser().getStore().getName();
                model.addAttribute("isScopedToStore", true);
                model.addAttribute("currentStoreName", storeName);
                model.addAttribute("currentStoreId", staffStoreId);

                stats = dashboardService.getDashboardStatisticsByStore(staffStoreId);
                model.addAttribute("stats", stats);
                model.addAttribute("stores", List.of(userDetails.getUser().getStore()));

                totalInventoryValue = storeValues.getOrDefault(storeName, BigDecimal.ZERO);

                try {
                    Map<String, Long> staffInv = Map.of(storeName, stats.getOrDefault("totalBookCopies", 0L));
                    Map<String, BigDecimal> staffVal = Map.of(storeName, totalInventoryValue);

                    model.addAttribute("inventoryByStoreJson", objectMapper.writeValueAsString(staffInv));
                    model.addAttribute("inventoryValueJson", objectMapper.writeValueAsString(staffVal));
                    model.addAttribute("conditionBreakdownJson", objectMapper.writeValueAsString(dashboardService.getConditionBreakdownByStore(staffStoreId)));
                    model.addAttribute("buybackStatusJson", objectMapper.writeValueAsString(dashboardService.getBuybackStatusBreakdownByStore(staffStoreId)));
                } catch (JsonProcessingException e) {
                    log.error("Lỗi chuyển đổi dữ liệu thống kê JSON cho STAFF", e);
                    setDefaultChartData(model);
                }
            }
        } else {
            // ADMIN & MANAGER: Xem toàn hệ thống
            model.addAttribute("isScopedToStore", false);
            if (userDetails != null && userDetails.getUser().getStore() != null) {
                model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
                model.addAttribute("currentStoreId", userDetails.getUser().getStore().getId());
            }

            stats = dashboardService.getDashboardStatistics();
            model.addAttribute("stats", stats);
            model.addAttribute("stores", storeRepository.findAll());

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

        // Danh sách yêu cầu thu mua & phiếu điều chuyển gần đây để hiển thị trực tiếp trên Dashboard
        List<BuybackRequest> recentBuybacks;
        List<StockTransfer> recentTransfers;
        if (isStaff) {
            if (staffStoreId == null) {
                recentBuybacks = List.of();
                recentTransfers = List.of();
            } else {
                recentBuybacks = buybackRequestRepository.searchRequests(staffStoreId, null, PageRequest.of(0, 5, Sort.by("id").descending())).getContent();
                recentTransfers = stockTransferRepository.searchTransfers(staffStoreId, null, PageRequest.of(0, 5, Sort.by("id").descending())).getContent();
            }
        } else {
            recentBuybacks = buybackRequestRepository.searchRequests(null, null, PageRequest.of(0, 5, Sort.by("id").descending())).getContent();
            recentTransfers = stockTransferRepository.searchTransfers(null, null, PageRequest.of(0, 5, Sort.by("id").descending())).getContent();
        }
        model.addAttribute("recentBuybacks", recentBuybacks);
        model.addAttribute("recentTransfers", recentTransfers);

        return "admin/dashboard";
    }

    private void setDefaultChartData(Model model) {
        model.addAttribute("inventoryByStoreJson", "{}");
        model.addAttribute("inventoryValueJson", "{}");
        model.addAttribute("conditionBreakdownJson", "{}");
        model.addAttribute("buybackStatusJson", "{}");
    }
}
