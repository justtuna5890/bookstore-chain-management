package com.bookstorechain.controller.web;

import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Cung cấp dữ liệu chung cho giao diện khách hàng (hiện tại: số sách trong giỏ hiển thị ở thanh trên). */
@ControllerAdvice(basePackages = "com.bookstorechain.controller.web")
@RequiredArgsConstructor
public class CustomerModelAdvice {

    private final CartService cartService;

    @ModelAttribute("cartCount")
    public long cartCount(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request) {
        if (userDetails == null || request.getRequestURI().startsWith("/admin")) {
            return 0L;
        }
        return cartService.countItems(userDetails.getUser().getId());
    }
}
