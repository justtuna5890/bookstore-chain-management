package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.CheckoutRequestDTO;
import com.bookstorechain.dto.response.CartDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.CartService;
import com.bookstorechain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping
    public String showCheckout(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        CartDTO cart = cartService.getCart(user.getId());
        if (cart.getAvailableCount() == 0) {
            return "redirect:/cart";
        }
        CheckoutRequestDTO form = CheckoutRequestDTO.builder()
                .receiverName(user.getFullName())
                .receiverPhone(user.getPhone())
                .build();
        populate(model, cart, form);
        return "customer/checkout";
    }

    @PostMapping
    public String placeOrder(@Valid @ModelAttribute("form") CheckoutRequestDTO form,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        Long userId = userDetails.getUser().getId();
        String error = null;

        if (bindingResult.hasErrors()) {
            List<String> messages = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            model.addAttribute("formErrors", messages);
        } else {
            try {
                List<OrderSummaryDTO> placed = orderService.checkout(userId, form);
                redirectAttributes.addFlashAttribute("placedOrders", placed);
                return "redirect:/checkout/success";
            } catch (BusinessException e) {
                error = e.getMessage();
            } catch (PessimisticLockingFailureException e) {
                error = "Hệ thống đang bận, vui lòng thử lại sau vài giây.";
            }
        }

        CartDTO cart = cartService.getCart(userId);
        if (cart.getAvailableCount() == 0) {
            redirectAttributes.addFlashAttribute("cartNotice", error != null ? error : "Giỏ hàng không còn sách khả dụng.");
            return "redirect:/cart";
        }
        model.addAttribute("checkoutError", error);
        populate(model, cart, form);
        return "customer/checkout";
    }

    @GetMapping("/success")
    public String success(Model model) {
        if (!model.containsAttribute("placedOrders")) {
            return "redirect:/";
        }
        return "customer/checkout-success";
    }

    private void populate(Model model, CartDTO cart, CheckoutRequestDTO form) {
        model.addAttribute("cart", cart);
        model.addAttribute("form", form);
        model.addAttribute("deliveryFee", orderService.getDeliveryFee());
    }
}
