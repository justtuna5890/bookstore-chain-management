package com.bookstorechain.controller.web;

import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
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
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public String view(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("items", wishlistService.getItems(userDetails.getUser().getId()));
        return "customer/wishlist";
    }

    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggle(@RequestParam Long bookId,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            boolean wishlisted = wishlistService.toggle(userDetails.getUser().getId(), bookId);
            return ResponseEntity.ok(Map.of("success", true, "wishlisted", wishlisted,
                    "message", wishlisted ? "Đã thêm vào danh sách yêu thích" : "Đã bỏ khỏi danh sách yêu thích"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (PessimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "Hệ thống đang bận, vui lòng thử lại sau vài giây."));
        }
    }
}
