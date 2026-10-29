package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.ChatConversationDTO;
import com.bookstorechain.dto.response.ChatMessageDTO;
import com.bookstorechain.dto.response.StoreResponseDTO;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.ChatService;
import com.bookstorechain.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Chat hỗ trợ phía khách: trang chat và các điểm cuối JSON mà trang gọi. Tin mới của nhân viên về qua /user/queue/chat. */
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final StoreService storeService;

    /** conversation mở sẵn một cuộc trò chuyện; store chọn sẵn một chi nhánh để bắt đầu (VD từ trang đơn hàng). */
    @GetMapping
    public String page(@RequestParam(value = "conversation", required = false) Long conversation,
                       @RequestParam(value = "store", required = false) Long store, Model model) {
        List<StoreResponseDTO> activeStores = storeService.getAllStores().stream()
                .filter(StoreResponseDTO::isActive).collect(Collectors.toList());
        model.addAttribute("stores", activeStores);
        model.addAttribute("initialConversationId", conversation);
        model.addAttribute("initialStoreId", store);
        return "customer/chat";
    }

    @GetMapping("/api/conversations")
    @ResponseBody
    public Map<String, Object> conversations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        List<ChatConversationDTO> list = chatService.getCustomerConversations(userId);
        return Map.of("conversations", list, "unread", chatService.countCustomerUnread(userId));
    }

    @GetMapping("/api/conversations/{id}/messages")
    @ResponseBody
    public List<ChatMessageDTO> messages(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return chatService.getCustomerMessages(userDetails.getUser().getId(), id);
    }

    @PostMapping("/api/conversations/{id}/messages")
    @ResponseBody
    public ChatMessageDTO send(@PathVariable Long id, @RequestParam String content,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        return chatService.sendInConversation(userDetails.getUser().getId(), id, content);
    }

    /** Bắt đầu cuộc trò chuyện mới với một chi nhánh (hoặc nhắn tiếp nếu đã có). */
    @PostMapping("/api/stores/{storeId}/messages")
    @ResponseBody
    public ChatMessageDTO start(@PathVariable Long storeId, @RequestParam String content,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        return chatService.sendFromCustomer(userDetails.getUser().getId(), storeId, content);
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
