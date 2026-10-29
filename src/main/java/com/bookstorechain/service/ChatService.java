package com.bookstorechain.service;

import com.bookstorechain.dto.response.ChatConversationDTO;
import com.bookstorechain.dto.response.ChatMessageDTO;
import com.bookstorechain.enums.ChatStatus;

import java.util.List;

/**
 * Chat hỗ trợ giữa khách và nhân viên của từng chi nhánh. Tin nhắn được lưu rồi đẩy realtime sau khi commit:
 * khách nhận ở /user/queue/chat, nhân viên ở /topic/chat/store/{id}, quản lý và quản trị còn có /topic/chat/all.
 * Truy cập cuộc trò chuyện không thuộc quyền được coi như không tồn tại (ResourceNotFoundException).
 */
public interface ChatService {

    // ---- Phía khách ----

    List<ChatConversationDTO> getCustomerConversations(Long customerId);

    /** Các tin của một cuộc trò chuyện của chính khách (cũ trước); đánh dấu khách đã đọc. */
    List<ChatMessageDTO> getCustomerMessages(Long customerId, Long conversationId);

    /** Bắt đầu hoặc tiếp tục cuộc trò chuyện với một chi nhánh; mở lại nếu đã đóng. */
    ChatMessageDTO sendFromCustomer(Long customerId, Long storeId, String content);

    /** Nhắn tiếp trong một cuộc trò chuyện sẵn có của khách. */
    ChatMessageDTO sendInConversation(Long customerId, Long conversationId, String content);

    long countCustomerUnread(Long customerId);

    // ---- Phía nhân viên ----

    /**
     * Hộp thư nhân viên, cuộc có tin mới nhất lên đầu. scopeStoreId khác null là nhân viên bị giới hạn ở chi nhánh đó
     * (bỏ qua storeFilter); scopeStoreId null (quản lý, quản trị) thì có thể lọc theo storeFilter. status null là mọi trạng thái.
     */
    List<ChatConversationDTO> getStaffConversations(Long scopeStoreId, Long storeFilter, ChatStatus status);

    /** Các tin của một cuộc trò chuyện (cũ trước); đánh dấu nhân viên đã đọc. */
    List<ChatMessageDTO> getStaffMessages(Long conversationId, Long scopeStoreId);

    ChatMessageDTO sendFromStaff(Long staffId, Long conversationId, Long scopeStoreId, String content);

    /** Đóng cuộc trò chuyện đã giải quyết; khách nhắn lại thì tự mở lại. */
    ChatConversationDTO close(Long conversationId, Long scopeStoreId);

    /** Số cuộc trò chuyện có tin khách chưa đọc trong phạm vi chi nhánh (null là mọi chi nhánh). */
    long countStaffUnread(Long scopeStoreId);
}
