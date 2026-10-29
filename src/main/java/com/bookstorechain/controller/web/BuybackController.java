package com.bookstorechain.controller.web;

import com.bookstorechain.entity.BuybackItem;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.BuybackService;
import com.bookstorechain.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        boolean isStaff = userDetails != null && (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER);

        if (isStaff) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                model.addAttribute("unassignedStoreWarning", true);
                model.addAttribute("requestPage", Page.empty());
                model.addAttribute("stores", List.of());
                model.addAttribute("statuses", BuybackStatus.values());
                return "admin/buyback/buyback-list";
            }
            storeId = staffStoreId;
            model.addAttribute("isStaffScoped", true);
            model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
            model.addAttribute("stores", List.of(userDetails.getUser().getStore()));
        } else {
            model.addAttribute("stores", storeService.getAllStores());
        }

        model.addAttribute("requestPage", buybackService.getRequests(storeId, status, PageRequest.of(page, 10, Sort.by("id").descending())));
        model.addAttribute("statuses", BuybackStatus.values());
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedStatus", status);
        return "admin/buyback/buyback-list";
    }

    @GetMapping("/{id}")
    public String viewDetail(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        BuybackRequest request = buybackService.getRequestById(id);
        boolean isStaff = userDetails != null && (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER);

        if (isStaff) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null || !staffStoreId.equals(request.getStore().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên chỉ có quyền xem đơn thu mua của chi nhánh mình phụ trách!");
                return "redirect:/admin/buybacks";
            }
            model.addAttribute("isStaffScoped", true);
            model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
        }

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
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            BuybackRequest request = buybackService.getRequestById(id);
            boolean isStaff = userDetails != null && (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER);

            // 1. Kiểm tra phạm vi chi nhánh của nhân viên
            if (isStaff) {
                Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
                if (staffStoreId == null || !staffStoreId.equals(request.getStore().getId())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên chỉ có quyền thẩm định và xử lý đơn thu mua của chi nhánh mình!");
                    return "redirect:/admin/buybacks/" + id;
                }
            }

            // 2. Ràng buộc quy trình thu mua: Không được nhảy cóc từ PENDING trực tiếp sang COMPLETED
            if (request.getStatus() == BuybackStatus.PENDING && status == BuybackStatus.COMPLETED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Đơn thu mua phải được thẩm định và duyệt định giá (APPROVED) trước khi hoàn tất nhập kho (COMPLETED)!");
                return "redirect:/admin/buybacks/" + id;
            }

            // 3. Nếu chuyển sang COMPLETED, kiểm tra ít nhất 1 item có giá thẩm định > 0
            if (status == BuybackStatus.COMPLETED) {
                boolean hasValidPricing = false;
                if (offeredPrices != null && !offeredPrices.isEmpty()) {
                    hasValidPricing = offeredPrices.stream().anyMatch(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0);
                } else if (request.getItems() != null) {
                    hasValidPricing = request.getItems().stream().anyMatch(i -> i.getOfferedPrice() != null && i.getOfferedPrice().compareTo(BigDecimal.ZERO) > 0);
                }
                if (!hasValidPricing) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể hoàn tất đơn thu mua khi chưa có cuốn sách nào được thẩm định giá hợp lệ (> 0 VNĐ)!");
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
