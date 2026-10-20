package com.bookstorechain.controller.web;

import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.service.DashboardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String showDashboard(@org.springframework.security.core.annotation.AuthenticationPrincipal com.bookstorechain.security.CustomUserDetails userDetails,
                                Model model) {
        boolean isStaff = userDetails != null && userDetails.getUser().getRole() == com.bookstorechain.enums.Role.STAFF;
        Long staffStoreId = isStaff && userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;

        if (isStaff && staffStoreId != null) {
            // Chỉ hiển thị dữ liệu của chi nhánh nhân viên phụ trách
            model.addAttribute("isScopedToStore", true);
            model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
            model.addAttribute("currentStoreId", staffStoreId);
            model.addAttribute("stats", dashboardService.getDashboardStatisticsByStore(staffStoreId));
            model.addAttribute("stores", java.util.List.of(userDetails.getUser().getStore()));

            try {
                // Biểu đồ 1 & 2: Chỉ hiển thị chi nhánh của nhân viên
                Map<String, Long> staffInv = Map.of(userDetails.getUser().getStore().getName(),
                        ((Number) dashboardService.getDashboardStatisticsByStore(staffStoreId).get("totalBookCopies")).longValue());
                Map<String, BigDecimal> staffVal = Map.of(userDetails.getUser().getStore().getName(),
                        dashboardService.getInventoryValueByStore().getOrDefault(userDetails.getUser().getStore().getName(), BigDecimal.ZERO));

                model.addAttribute("inventoryByStoreJson", objectMapper.writeValueAsString(staffInv));
                model.addAttribute("inventoryValueJson", objectMapper.writeValueAsString(staffVal));
                model.addAttribute("conditionBreakdownJson", objectMapper.writeValueAsString(dashboardService.getConditionBreakdownByStore(staffStoreId)));
                model.addAttribute("buybackStatusJson", objectMapper.writeValueAsString(dashboardService.getBuybackStatusBreakdownByStore(staffStoreId)));
            } catch (JsonProcessingException e) {
                log.error("Loi chuyen du lieu thong ke sang JSON", e);
                model.addAttribute("inventoryByStoreJson", "{}");
                model.addAttribute("inventoryValueJson", "{}");
                model.addAttribute("conditionBreakdownJson", "{}");
                model.addAttribute("buybackStatusJson", "{}");
            }
        } else {
            // ADMIN / MANAGER: Xem toàn hệ thống
            model.addAttribute("isScopedToStore", false);
            model.addAttribute("stats", dashboardService.getDashboardStatistics());
            model.addAttribute("stores", storeRepository.findAll());

            try {
                model.addAttribute("inventoryByStoreJson", objectMapper.writeValueAsString(dashboardService.getInventoryByStore()));
                model.addAttribute("inventoryValueJson", objectMapper.writeValueAsString(dashboardService.getInventoryValueByStore()));
                model.addAttribute("conditionBreakdownJson", objectMapper.writeValueAsString(dashboardService.getConditionBreakdown()));
                model.addAttribute("buybackStatusJson", objectMapper.writeValueAsString(dashboardService.getBuybackStatusBreakdown()));
            } catch (JsonProcessingException e) {
                log.error("Loi chuyen du lieu thong ke sang JSON", e);
                model.addAttribute("inventoryByStoreJson", "{}");
                model.addAttribute("inventoryValueJson", "{}");
                model.addAttribute("conditionBreakdownJson", "{}");
                model.addAttribute("buybackStatusJson", "{}");
            }
        }

        return "admin/dashboard";
    }
}