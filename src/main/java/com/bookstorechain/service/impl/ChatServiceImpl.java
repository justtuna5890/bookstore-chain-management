package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ChatConversationDTO;
import com.bookstorechain.dto.response.ChatEventDTO;
import com.bookstorechain.dto.response.ChatMessageDTO;
import com.bookstorechain.dto.response.NotificationDTO;
import com.bookstorechain.entity.ChatConversation;
import com.bookstorechain.entity.ChatMessage;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.ChatStatus;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.ChatConversationRepository;
import com.bookstorechain.repository.ChatMessageRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.ChatService;
import com.bookstorechain.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    /** Đích gửi cho từng khách: Spring tự đổi thành /user/{tên đăng nhập}/queue/chat. */
    static final String CUSTOMER_QUEUE = "/queue/chat";
    static final String STORE_TOPIC_PREFIX = "/topic/chat/store/";
    static final String ALL_STORES_TOPIC = "/topic/chat/all";
    /** Kênh thông báo của nhân viên (chuông ở thanh quản trị) dùng chung với đơn hàng mới. */
    static final String STAFF_BELL_STORE_PREFIX = "/topic/store/";
    static final String STAFF_BELL_ALL = "/topic/store/all";

    static final int MAX_CONTENT = 1000;
    static final int MAX_PREVIEW = 100;
    static final int MESSAGE_PAGE = 200;
    static final int INBOX_LIMIT = 100;
    static final int MAX_MESSAGES_PER_MINUTE = 20;

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserNotificationService userNotificationService;
    private final UserLocks userLocks;
    private final PlatformTransactionManager transactionManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    // ---------------------------------------------------------------- phía khách

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDTO> getCustomerConversations(Long customerId) {
        return conversationRepository.findByCustomer(customerId).stream()
                .map(conversation -> toConversationDTO(conversation, true)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ChatMessageDTO> getCustomerMessages(Long customerId, Long conversationId) {
        // Khoá trước rồi mới đọc để tin vừa commit nằm trong danh sách và không bị số chưa đọc ghi đè
        ChatConversation conversation = conversationRepository.lockById(conversationId)
                .filter(c -> c.getCustomer().getId().equals(customerId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện."));
        if (conversation.getCustomerUnread() > 0) {
            conversation.setCustomerUnread(0);
            conversationRepository.save(conversation);
        }
        List<ChatMessageDTO> messages = messagesOf(conversation);
        // Thông báo "nhân viên đã trả lời" trên chuông không còn ý nghĩa khi khách đã mở cuộc chat
        userNotificationService.markReadByLink(customerId, chatLink(conversationId));
        return messages;
    }

    @Override
    public ChatMessageDTO sendFromCustomer(Long customerId, Long storeId, String content) {
        String text = normalize(content);
        // Khoá theo khách ngoài transaction: hai tin đầu tiên gửi cùng lúc không tạo trùng cuộc trò chuyện (ràng buộc duy nhất)
        synchronized (userLocks.lockFor(customerId)) {
            return new TransactionTemplate(transactionManager).execute(status -> doCustomerSend(customerId, storeId, text));
        }
    }

    @Override
    public ChatMessageDTO sendInConversation(Long customerId, Long conversationId, String content) {
        String text = normalize(content);
        synchronized (userLocks.lockFor(customerId)) {
            return new TransactionTemplate(transactionManager).execute(status -> {
                // Tra bằng truy vấn trả về giá trị đơn để chưa nạp cuộc trò chuyện trước khi khoá (xem lockById)
                Long storeId = conversationRepository.findStoreIdOwnedBy(conversationId, customerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện."));
                return doCustomerSend(customerId, storeId, text);
            });
        }
    }

    private ChatMessageDTO doCustomerSend(Long customerId, Long storeId, String text) {
        checkRate(customerId);
        Store store = storeRepository.findById(storeId)
                .filter(Store::isActive)
                .orElseThrow(() -> new BusinessException("Chi nhánh không tồn tại hoặc đã ngừng hoạt động."));
        Long conversationId = conversationRepository.findIdByCustomerAndStore(customerId, storeId)
                .orElseGet(() -> conversationRepository.save(ChatConversation.builder()
                        .customer(userRepository.getReferenceById(customerId)).store(store).build()).getId());
        ChatConversation conversation = conversationRepository.lockById(conversationId).orElseThrow();

        boolean firstUnreadForStaff = conversation.getStaffUnread() == 0;
        ChatMessage message = messageRepository.save(ChatMessage.builder()
                .conversation(conversation).sender(userRepository.getReferenceById(customerId)).fromCustomer(true).content(text).build());
        conversation.setStatus(ChatStatus.OPEN);
        conversation.setStaffUnread(conversation.getStaffUnread() + 1);
        touch(conversation, text);
        conversationRepository.save(conversation);

        ChatMessageDTO dto = toMessageDTO(message, conversation);
        ChatEventDTO staffEvent = ChatEventDTO.builder().conversation(toConversationDTO(conversation, false)).message(dto).build();
        pushToStaff(conversation.getStore().getId(), staffEvent);
        if (firstUnreadForStaff) {
            ringStaffBell(conversation, text);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public long countCustomerUnread(Long customerId) {
        return conversationRepository.countWithCustomerUnread(customerId);
    }

    // ------------------------------------------------------------ phía nhân viên

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDTO> getStaffConversations(Long scopeStoreId, Long storeFilter, ChatStatus status) {
        Long storeId = scopeStoreId != null ? scopeStoreId : storeFilter;
        return conversationRepository.findForStaff(storeId, status, PageRequest.of(0, INBOX_LIMIT)).stream()
                .map(conversation -> toConversationDTO(conversation, false)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ChatMessageDTO> getStaffMessages(Long conversationId, Long scopeStoreId) {
        ChatConversation conversation = lockInScope(conversationId, scopeStoreId);
        if (conversation.getStaffUnread() > 0) {
            conversation.setStaffUnread(0);
            conversationRepository.save(conversation);
        }
        return messagesOf(conversation);
    }

    @Override
    @Transactional
    public ChatMessageDTO sendFromStaff(Long staffId, Long conversationId, Long scopeStoreId, String content) {
        String text = normalize(content);
        ChatConversation conversation = lockInScope(conversationId, scopeStoreId);
        if (conversation.getStatus() == ChatStatus.CLOSED) {
            throw new BusinessException("Cuộc trò chuyện đã đóng. Khách nhắn lại thì cuộc trò chuyện sẽ tự mở.");
        }
        checkRate(staffId);

        boolean firstUnreadForCustomer = conversation.getCustomerUnread() == 0;
        ChatMessage message = messageRepository.save(ChatMessage.builder()
                .conversation(conversation).sender(userRepository.getReferenceById(staffId)).fromCustomer(false).content(text).build());
        conversation.setCustomerUnread(conversation.getCustomerUnread() + 1);
        conversation.setStaffUnread(0);
        touch(conversation, text);
        conversationRepository.save(conversation);

        ChatMessageDTO dto = toMessageDTO(message, conversation);
        publish(conversation, dto);
        if (firstUnreadForCustomer) {
            userNotificationService.notify(conversation.getCustomer().getId(), UserNotificationType.CHAT,
                    "Tin nhắn mới từ " + conversation.getStore().getName(), preview(text), chatLink(conversation.getId()));
        }
        return dto;
    }

    @Override
    @Transactional
    public ChatConversationDTO close(Long conversationId, Long scopeStoreId) {
        ChatConversation conversation = lockInScope(conversationId, scopeStoreId);
        conversation.setStatus(ChatStatus.CLOSED);
        conversationRepository.save(conversation);
        // Khách và đồng nghiệp cùng chi nhánh thấy trạng thái đóng ngay, không cần tải lại
        publish(conversation, null);
        return toConversationDTO(conversation, false);
    }

    @Override
    @Transactional(readOnly = true)
    public long countStaffUnread(Long scopeStoreId) {
        return conversationRepository.countWithStaffUnread(scopeStoreId);
    }

    // ------------------------------------------------------------------ nội bộ

    /** Khoá cuộc trò chuyện và kiểm tra quyền; ngoài phạm vi chi nhánh thì coi như không tồn tại để không lộ id. */
    private ChatConversation lockInScope(Long conversationId, Long scopeStoreId) {
        return conversationRepository.lockById(conversationId)
                .filter(c -> scopeStoreId == null || scopeStoreId.equals(c.getStore().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện."));
    }

    private String normalize(String content) {
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new BusinessException("Vui lòng nhập nội dung tin nhắn.");
        }
        if (text.length() > MAX_CONTENT) {
            throw new BusinessException("Tin nhắn tối đa " + MAX_CONTENT + " ký tự.");
        }
        return text;
    }

    private void checkRate(Long senderId) {
        LocalDateTime since = LocalDateTime.now(clock).minusMinutes(1);
        if (messageRepository.countBySenderIdAndCreatedAtAfter(senderId, since) >= MAX_MESSAGES_PER_MINUTE) {
            throw new BusinessException("Bạn gửi tin nhắn quá nhanh, vui lòng chờ một lát rồi thử lại.");
        }
    }

    private void touch(ChatConversation conversation, String text) {
        conversation.setLastMessageAt(LocalDateTime.now(clock));
        conversation.setLastMessagePreview(preview(text));
    }

    private String preview(String text) {
        String flat = text.replaceAll("\\s+", " ");
        return flat.length() <= MAX_PREVIEW ? flat : flat.substring(0, MAX_PREVIEW - 1) + "…";
    }

    private String chatLink(Long conversationId) {
        return "/chat?conversation=" + conversationId;
    }

    private List<ChatMessageDTO> messagesOf(ChatConversation conversation) {
        List<ChatMessage> latest = new ArrayList<>(messageRepository.findLatest(conversation.getId(), PageRequest.of(0, MESSAGE_PAGE)));
        Collections.reverse(latest);
        return latest.stream().map(message -> toMessageDTO(message, conversation)).collect(Collectors.toList());
    }

    /** Đẩy sự kiện tới khách (kênh riêng) và nhân viên chi nhánh (kênh chi nhánh và kênh tất cả); message null là chỉ đổi trạng thái. */
    private void publish(ChatConversation conversation, ChatMessageDTO message) {
        String customerName = conversation.getCustomer().getUsername();
        ChatEventDTO customerEvent = ChatEventDTO.builder().conversation(toConversationDTO(conversation, true)).message(message).build();
        ChatEventDTO staffEvent = ChatEventDTO.builder().conversation(toConversationDTO(conversation, false)).message(message).build();
        Long storeId = conversation.getStore().getId();
        AfterCommit.run(() -> {
            send(() -> messagingTemplate.convertAndSendToUser(customerName, CUSTOMER_QUEUE, customerEvent), "khách " + customerName);
            sendToStaffTopics(storeId, staffEvent);
        });
    }

    private void pushToStaff(Long storeId, ChatEventDTO event) {
        AfterCommit.run(() -> sendToStaffTopics(storeId, event));
    }

    private void sendToStaffTopics(Long storeId, ChatEventDTO event) {
        send(() -> messagingTemplate.convertAndSend(STORE_TOPIC_PREFIX + storeId, event), "chi nhánh " + storeId);
        send(() -> messagingTemplate.convertAndSend(ALL_STORES_TOPIC, event), "tất cả chi nhánh");
    }

    /** Tin khách đầu tiên chưa đọc: rung chuông ở thanh quản trị của chi nhánh (cùng kênh với đơn hàng mới). */
    private void ringStaffBell(ChatConversation conversation, String text) {
        Store store = conversation.getStore();
        NotificationDTO bell = NotificationDTO.builder()
                .id(UUID.randomUUID().toString())
                .title("Tin nhắn hỗ trợ mới")
                .message(conversation.getCustomer().getFullName() + ": " + preview(text))
                .type("CHAT")
                .storeId(store.getId())
                .storeName(store.getName())
                .targetUrl("/admin/chat?conversation=" + conversation.getId())
                .timestamp(LocalDateTime.now(clock))
                .build();
        AfterCommit.run(() -> {
            send(() -> messagingTemplate.convertAndSend(STAFF_BELL_STORE_PREFIX + store.getId(), bell), "chuông chi nhánh " + store.getId());
            send(() -> messagingTemplate.convertAndSend(STAFF_BELL_ALL, bell), "chuông tất cả chi nhánh");
        });
    }

    /** Lỗi WebSocket không được làm hỏng tin đã lưu. */
    private void send(Runnable action, String target) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Lỗi khi đẩy chat WebSocket tới {}", target, e);
        }
    }

    private ChatConversationDTO toConversationDTO(ChatConversation conversation, boolean forCustomer) {
        return ChatConversationDTO.builder()
                .id(conversation.getId())
                .storeId(conversation.getStore().getId())
                .storeName(conversation.getStore().getName())
                .customerId(conversation.getCustomer().getId())
                .customerName(conversation.getCustomer().getFullName())
                .status(conversation.getStatus())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt())
                .timeAgo(RelativeTime.ago(conversation.getLastMessageAt(), LocalDateTime.now(clock)))
                .unread(forCustomer ? conversation.getCustomerUnread() : conversation.getStaffUnread())
                .build();
    }

    private ChatMessageDTO toMessageDTO(ChatMessage message, ChatConversation conversation) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .conversationId(conversation.getId())
                .fromCustomer(message.isFromCustomer())
                .senderName(message.isFromCustomer() ? conversation.getCustomer().getFullName() : "Nhân viên " + conversation.getStore().getName())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
