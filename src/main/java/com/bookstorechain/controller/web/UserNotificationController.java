package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.UserNotificationDTO;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/** Thông báo riêng của người đang đăng nhập: trang đầy đủ và các điểm cuối JSON cho chuông trên thanh trên. */
@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private static final int PAGE_SIZE = 15;
    private static final int RECENT_LIMIT = 8;

    private final UserNotificationService userNotificationService;

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        Page<UserNotificationDTO> notifications = userNotificationService.getPage(userId, PageRequest.of(Math.max(page, 0), PAGE_SIZE));
        model.addAttribute("notificationPage", notifications);
        model.addAttribute("unreadCount", userNotificationService.countUnread(userId));
        return "customer/notifications";
    }

    @GetMapping("/recent")
    @ResponseBody
    public Map<String, Object> recent(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        List<UserNotificationDTO> items = userNotificationService.getRecent(userId, RECENT_LIMIT);
        return Map.of("unread", userNotificationService.countUnread(userId), "items", items);
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        // Không phân biệt "không tồn tại" với "của người khác" để không lộ id thông báo của người dùng khác
        if (!userNotificationService.markRead(userId, id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "unread", userNotificationService.countUnread(userId)));
        }
        return ResponseEntity.ok(Map.of("success", true, "unread", userNotificationService.countUnread(userId)));
    }

    @PostMapping("/read-all")
    @ResponseBody
    public Map<String, Object> markAllRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        int updated = userNotificationService.markAllRead(userId);
        return Map.of("success", true, "updated", updated, "unread", userNotificationService.countUnread(userId));
    }
}
