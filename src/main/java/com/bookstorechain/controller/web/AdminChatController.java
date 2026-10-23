package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.ChatConversationDTO;
import com.bookstorechain.dto.response.ChatMessageDTO;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.ChatStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Hộp thư chat hỗ trợ cho nhân viên. Nhân viên chỉ thấy và trả lời chat của chi nhánh mình; quản lý và quản trị
 * thấy mọi chi nhánh (cùng quy ước với quản lý đơn hàng). Tin mới về qua /topic/chat/store/{id} hoặc /topic/chat/all.
 */
@Controller
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class AdminChatController {

    private final ChatService chatService;

    @GetMapping("/api/conversations")
    @ResponseBody
    public Map<String, Object> conversations(@RequestParam(value = "storeId", required = false) Long storeId,
                                             @RequestParam(value = "status", required = false) ChatStatus status,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long scope = scopeStoreId(userDetails);
        List<ChatConversationDTO> list = chatService.getStaffConversations(scope, storeId, status);
        return Map.of("conversations", list, "unread", chatService.countStaffUnread(scope));
    }

    @GetMapping("/api/conversations/{id}/messages")
    @ResponseBody
    public List<ChatMessageDTO> messages(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return chatService.getStaffMessages(id, scopeStoreId(userDetails));
    }

    @PostMapping("/api/conversations/{id}/messages")
    @ResponseBody
    public ChatMessageDTO reply(@PathVariable Long id, @RequestParam String content,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        return chatService.sendFromStaff(userDetails.getUser().getId(), id, scopeStoreId(userDetails), content);
    }

    @PostMapping("/api/conversations/{id}/close")
    @ResponseBody
    public ChatConversationDTO close(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return chatService.close(id, scopeStoreId(userDetails));
    }

    /** Chi nhánh giới hạn của người dùng: nhân viên là chi nhánh của mình, quản lý và quản trị là null (mọi chi nhánh). */
    private Long scopeStoreId(CustomUserDetails userDetails) {
        if (userDetails.getUser().getRole() != Role.STAFF) {
            return null;
        }
        Store store = userDetails.getUser().getStore();
        if (store == null) {
            throw new BusinessException("Tài khoản của bạn chưa được gán chi nhánh nên chưa xử lý được chat.");
        }
        return store.getId();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejected(BusinessException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
    }
}
