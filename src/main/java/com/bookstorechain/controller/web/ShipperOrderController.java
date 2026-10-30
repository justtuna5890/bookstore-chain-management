package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.ShipperOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
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

/** Khu giao hàng của shipper, thiết kế cho điện thoại. Mọi thao tác dùng id của người đăng nhập, không nhận id shipper từ request. */
@Controller
@RequestMapping("/shipper/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHIPPER')")
public class ShipperOrderController {

    private static final String TAB_AVAILABLE = "available";
    private static final String TAB_MINE = "mine";
    private static final String TAB_HISTORY = "history";

    private final ShipperOrderService shipperOrderService;

    @GetMapping
    public String list(@RequestParam(value = "tab", required = false) String tab,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model) {
        Long shipperId = userDetails.getUser().getId();
        model.addAttribute("shipperName", userDetails.getUser().getFullName());
        model.addAttribute("storeName", userDetails.getUser().getStore() == null ? null : userDetails.getUser().getStore().getName());

        List<OrderSummaryDTO> orders = List.of();
        String activeTab = TAB_MINE.equals(tab) || TAB_HISTORY.equals(tab) ? tab : TAB_AVAILABLE;
        try {
            model.addAttribute("availableCount", shipperOrderService.findAvailable(shipperId).size());
            model.addAttribute("mineCount", shipperOrderService.findMine(shipperId).size());
            orders = switch (activeTab) {
                case TAB_MINE -> shipperOrderService.findMine(shipperId);
                case TAB_HISTORY -> shipperOrderService.findHistory(shipperId);
                default -> shipperOrderService.findAvailable(shipperId);
            };
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("tab", activeTab);
        model.addAttribute("orders", orders);
        return "shipper/order-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        try {
            model.addAttribute("order", shipperOrderService.getOrder(id, userDetails.getUser().getId()));
            model.addAttribute("shipperName", userDetails.getUser().getFullName());
            return "shipper/order-detail";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/shipper/orders";
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping("/{id}/claim")
    public String claim(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                        RedirectAttributes redirectAttributes) {
        try {
            shipperOrderService.claim(id, userDetails.getUser().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã nhận đơn. Chúc bạn giao hàng thuận lợi!");
            return "redirect:/shipper/orders/" + id;
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (PessimisticLockingFailureException e) {
            redirectAttributes.addFlashAttribute("errorMessage", busyMessage());
        }
        return "redirect:/shipper/orders";
    }

    @PostMapping("/{id}/deliver")
    public String deliver(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Đã ghi nhận giao hàng thành công.",
                () -> shipperOrderService.deliver(id, userDetails.getUser().getId()));
    }

    @PostMapping("/{id}/fail")
    public String fail(@PathVariable Long id,
                       @RequestParam(value = "reason", required = false) String reason,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Đã ghi nhận giao không thành công, chi nhánh sẽ xử lý tiếp.",
                () -> shipperOrderService.fail(id, userDetails.getUser().getId(), reason));
    }

    private String act(Long id, RedirectAttributes redirectAttributes, String successMessage, Runnable action) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (BusinessException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (PessimisticLockingFailureException e) {
            redirectAttributes.addFlashAttribute("errorMessage", busyMessage());
        }
        return "redirect:/shipper/orders/" + id;
    }

    private String busyMessage() {
        return "Hệ thống đang bận, vui lòng thử lại sau vài giây.";
    }
}
