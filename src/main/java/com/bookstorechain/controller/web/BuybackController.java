package com.bookstorechain.controller.web;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.service.BuybackService;
import com.bookstorechain.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/buybacks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class BuybackController {

    private final BuybackService buybackService;
    private final StoreService storeService;

    @GetMapping
    public String listRequests(@RequestParam(value = "storeId", required = false) Long storeId,
                               @RequestParam(value = "status", required = false) BuybackStatus status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        model.addAttribute("requestPage", buybackService.getRequests(storeId, status, PageRequest.of(page, 10, Sort.by("id").descending())));
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("statuses", BuybackStatus.values());
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedStatus", status);
        return "admin/buyback/buyback-list";
    }

    @GetMapping("/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        BuybackRequest request = buybackService.getRequestById(id);
        model.addAttribute("buybackRequest", request);
        model.addAttribute("statuses", BuybackStatus.values());
        return "admin/buyback/buyback-detail";
    }

    @PostMapping("/{id}/update")
    public String updateRequest(@PathVariable Long id,
                                @RequestParam("status") BuybackStatus status,
                                @RequestParam(value = "staffNote", required = false) String staffNote,
                                @RequestParam(value = "itemIds", required = false) List<Long> itemIds,
                                @RequestParam(value = "offeredPrices", required = false) List<BigDecimal> offeredPrices,
                                @org.springframework.security.core.annotation.AuthenticationPrincipal com.bookstorechain.security.CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            BuybackRequest request = buybackService.getRequestById(id);
            if (userDetails != null && userDetails.getUser().getRole() == com.bookstorechain.enums.Role.STAFF) {
                Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
                if (staffStoreId == null || !staffStoreId.equals(request.getStore().getId())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên chỉ có quyền thẩm định và duyệt đơn thu mua của chi nhánh mình!");
                    return "redirect:/admin/buybacks/" + id;
                }
            }

            buybackService.updateStatusAndPricing(id, status, staffNote, itemIds, offeredPrices);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật yêu cầu thu mua thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/buybacks/" + id;
    }
}