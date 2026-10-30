package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.AdminOrderService;
import com.bookstorechain.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class AdminOrderController {

    private static final int PAGE_SIZE = 10;

    private final AdminOrderService adminOrderService;
    private final StoreService storeService;

    @GetMapping
    public String list(@RequestParam(value = "storeId", required = false) Long storeId,
                       @RequestParam(value = "status", required = false) OrderStatus status,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model) {
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedKeyword", keyword);

        if (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER) {
            Store store = userDetails.getUser().getStore();
            if (store == null) {
                model.addAttribute("unassignedStoreWarning", true);
                model.addAttribute("orderPage", Page.empty());
                model.addAttribute("stores", List.of());
                return "admin/order/order-list";
            }
            storeId = store.getId();
            model.addAttribute("isStaffScoped", true);
            model.addAttribute("currentStoreName", store.getName());
            model.addAttribute("stores", List.of(store));
        } else {
            model.addAttribute("stores", storeService.getAllStores());
        }

        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("orderPage", adminOrderService.search(storeId, status, keyword,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("id").descending())));
        return "admin/order/order-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        try {
            OrderSummaryDTO order = adminOrderService.getOrder(id, scopeStoreId(userDetails));
            model.addAttribute("order", order);
            return "admin/order/order-detail";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/orders";
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Đã xác nhận đơn hàng.",
                () -> adminOrderService.confirm(id, scopeStoreId(userDetails)));
    }

    @PostMapping("/{id}/ready")
    public String markReady(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Đã đóng gói xong: đơn chờ khách đến nhận hoặc chờ shipper nhận.",
                () -> adminOrderService.markReady(id, scopeStoreId(userDetails)));
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Đã hoàn tất đơn hàng, các bản sách được ghi nhận là đã bán.",
                () -> adminOrderService.complete(id, scopeStoreId(userDetails)));
    }

    @PostMapping("/{id}/redeliver")
    public String redeliver(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Đã đưa đơn về danh sách chờ shipper nhận để giao lại.",
                () -> adminOrderService.redeliver(id, scopeStoreId(userDetails)));
    }

    @PostMapping("/{id}/payment")
    public String confirmPayment(@PathVariable Long id,
                                 @RequestParam(value = "transactionCode", required = false) String transactionCode,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Đã ghi nhận thanh toán chuyển khoản.",
                () -> adminOrderService.confirmPayment(id, scopeStoreId(userDetails), transactionCode));
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Đã huỷ đơn hàng, các bản sách được mở bán trở lại.",
                () -> adminOrderService.cancel(id, scopeStoreId(userDetails)));
    }

    /** Nhân viên chỉ xử lý đơn của chi nhánh mình; quản lý và quản trị không bị giới hạn (giống các trang thu mua, chuyển kho). */
    private Long scopeStoreId(CustomUserDetails userDetails) {
        if (userDetails.getUser().getRole() == Role.ADMIN) {
            return null;
        }
        Store store = userDetails.getUser().getStore();
        if (store == null) {
            throw new BusinessException("Tài khoản của bạn chưa được phân bổ chi nhánh công tác.");
        }
        return store.getId();
    }

    private String act(Long id, RedirectAttributes redirectAttributes, String successMessage, Runnable action) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (PessimisticLockingFailureException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hệ thống đang bận, vui lòng thử lại sau vài giây.");
        }
        return "redirect:/admin/orders/" + id;
    }
}
