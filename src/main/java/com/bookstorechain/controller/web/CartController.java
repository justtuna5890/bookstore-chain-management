package com.bookstorechain.controller.web;

import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("cart", cartService.getCart(userDetails.getUser().getId()));
        return "customer/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> add(@RequestParam Long copyId,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            int cartCount = cartService.addItem(userDetails.getUser().getId(), copyId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm vào giỏ hàng", "cartCount", cartCount));
        } catch (BusinessException | ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long copyId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.removeItem(userDetails.getUser().getId(), copyId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(@AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clear(userDetails.getUser().getId());
        return "redirect:/cart";
    }
}
