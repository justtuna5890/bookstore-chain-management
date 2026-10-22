package com.bookstorechain.controller.web;

import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final OrderService orderService;

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("orders", orderService.getCustomerOrders(userDetails.getUser().getId()));
        return "customer/order-list";
    }

    @GetMapping("/{code}")
    public String detail(@PathVariable String code, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        try {
            model.addAttribute("order", orderService.getCustomerOrder(userDetails.getUser().getId(), code));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        return "customer/order-detail";
    }

    @PostMapping("/{code}/cancel")
    public String cancel(@PathVariable String code,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelByCustomer(userDetails.getUser().getId(), code);
            redirectAttributes.addFlashAttribute("orderSuccess", "Đã huỷ đơn hàng " + code + ". Các cuốn sách được mở bán trở lại.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("orderError", e.getMessage());
        } catch (PessimisticLockingFailureException e) {
            redirectAttributes.addFlashAttribute("orderError", "Hệ thống đang bận, vui lòng thử lại sau vài giây.");
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        return "redirect:/orders/" + code;
    }
}
