package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ChatConversationDTO;
import com.bookstorechain.dto.response.ChatEventDTO;
import com.bookstorechain.dto.response.ChatMessageDTO;
import com.bookstorechain.dto.response.NotificationDTO;
import com.bookstorechain.entity.ChatConversation;
import com.bookstorechain.entity.ChatMessage;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.ChatStatus;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.ChatConversationRepository;
import com.bookstorechain.repository.ChatMessageRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.UserNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    private static final long CUSTOMER_ID = 7L;
    private static final long STAFF_ID = 3L;
    private static final long STORE_ID = 1L;
    private static final long CONVERSATION_ID = 40L;
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-23T10:00:00Z"), ZoneId.of("UTC"));

    @Mock private ChatConversationRepository conversationRepository;
    @Mock private ChatMessageRepository messageRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserNotificationService userNotificationService;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private ChatServiceImpl service;
    private User customer;
    private User staff;
    private Store store;

    @BeforeEach
    void setUp() {
        service = new ChatServiceImpl(conversationRepository, messageRepository, storeRepository, userRepository,
                userNotificationService, new UserLocks(), transactionManager, messagingTemplate, CLOCK);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        customer = TestData.customer(CUSTOMER_ID);
        staff = TestData.customer(STAFF_ID);
        store = TestData.store(STORE_ID, "OldBook Ha Noi");
        lenient().when(userRepository.getReferenceById(CUSTOMER_ID)).thenReturn(customer);
        lenient().when(userRepository.getReferenceById(STAFF_ID)).thenReturn(staff);
        lenient().when(messageRepository.save(any(ChatMessage.class))).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setId(900L);
            m.setCreatedAt(LocalDateTime.of(2026, 10, 23, 9, 59));
            return m;
        });
        lenient().when(conversationRepository.save(any(ChatConversation.class))).thenAnswer(inv -> {
            ChatConversation c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(CONVERSATION_ID);
            }
            return c;
        });
    }

    private ChatConversation conversation(ChatStatus status, int customerUnread, int staffUnread) {
        ChatConversation c = ChatConversation.builder().customer(customer).store(store).status(status)
                .customerUnread(customerUnread).staffUnread(staffUnread).build();
        c.setId(CONVERSATION_ID);
        lenient().when(conversationRepository.lockById(CONVERSATION_ID)).thenReturn(Optional.of(c));
        lenient().when(conversationRepository.findStoreIdOwnedBy(CONVERSATION_ID, CUSTOMER_ID)).thenReturn(Optional.of(STORE_ID));
        return c;
    }

    // ------------------------------------------------------------------ khách gửi

    @Test
    @DisplayName("Tin đầu tiên của khách tạo cuộc trò chuyện, tăng số chưa đọc của nhân viên và đẩy tới kênh chi nhánh")
    void firstCustomerMessageCreatesConversation() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(conversationRepository.findIdByCustomerAndStore(CUSTOMER_ID, STORE_ID)).thenReturn(Optional.empty());
        ChatConversation[] created = new ChatConversation[1];
        when(conversationRepository.lockById(CONVERSATION_ID)).thenAnswer(inv -> Optional.of(created[0]));
        when(conversationRepository.save(any(ChatConversation.class))).thenAnswer(inv -> {
            ChatConversation c = inv.getArgument(0);
            c.setId(CONVERSATION_ID);
            created[0] = c;
            return c;
        });

        ChatMessageDTO result = service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "  Sách này còn không ạ?  ");

        assertThat(result.getContent()).isEqualTo("Sách này còn không ạ?");
        assertThat(result.isFromCustomer()).isTrue();
        assertThat(result.getSenderName()).isEqualTo("Khach 7");
        assertThat(result.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(created[0].getStaffUnread()).isEqualTo(1);
        assertThat(created[0].getStatus()).isEqualTo(ChatStatus.OPEN);
        assertThat(created[0].getLastMessagePreview()).isEqualTo("Sách này còn không ạ?");
        assertThat(created[0].getLastMessageAt()).isEqualTo(LocalDateTime.of(2026, 10, 23, 10, 0));

        ArgumentCaptor<ChatEventDTO> event = ArgumentCaptor.forClass(ChatEventDTO.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/store/1"), event.capture());
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/all"), any(Object.class));
        assertThat(event.getValue().getConversation().getUnread()).as("số chưa đọc tính cho nhân viên").isEqualTo(1);
        assertThat(event.getValue().getMessage().getContent()).isEqualTo("Sách này còn không ạ?");
        verifyNoInteractions(userNotificationService);
    }

    @Test
    @DisplayName("Tin khách đầu tiên chưa đọc rung chuông ở thanh quản trị của chi nhánh, loại CHAT, trỏ tới cuộc chat")
    void firstUnreadRingsStaffBell() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 0);
        when(conversationRepository.findIdByCustomerAndStore(CUSTOMER_ID, STORE_ID)).thenReturn(Optional.of(CONVERSATION_ID));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

        service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "Xin chào");

        ArgumentCaptor<NotificationDTO> bell = ArgumentCaptor.forClass(NotificationDTO.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/store/1"), bell.capture());
        verify(messagingTemplate).convertAndSend(eq("/topic/store/all"), any(Object.class));
        assertThat(bell.getValue().getType()).isEqualTo("CHAT");
        assertThat(bell.getValue().getMessage()).contains("Khach 7").contains("Xin chào");
        assertThat(bell.getValue().getTargetUrl()).isEqualTo("/admin/chat?conversation=" + CONVERSATION_ID);
    }

    @Test
    @DisplayName("Khách nhắn tiếp khi nhân viên chưa đọc: cộng dồn số chưa đọc nhưng không rung chuông lại")
    void secondUnreadDoesNotRingAgain() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 2);
        when(conversationRepository.findIdByCustomerAndStore(CUSTOMER_ID, STORE_ID)).thenReturn(Optional.of(CONVERSATION_ID));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

        service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "Còn ai không?");

        assertThat(conversation.getStaffUnread()).isEqualTo(3);
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/store/1"), any(Object.class));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/store/1"), any(Object.class));
    }

    @Test
    @DisplayName("Khách nhắn lại vào cuộc đã đóng thì cuộc trò chuyện mở lại")
    void customerMessageReopensClosedConversation() {
        ChatConversation conversation = conversation(ChatStatus.CLOSED, 0, 0);
        when(conversationRepository.findIdByCustomerAndStore(CUSTOMER_ID, STORE_ID)).thenReturn(Optional.of(CONVERSATION_ID));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

        service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "Mình hỏi thêm chút");

        assertThat(conversation.getStatus()).isEqualTo(ChatStatus.OPEN);
    }

    @Test
    @DisplayName("Tin nhắn trống hoặc quá 1000 ký tự bị từ chối, không lưu gì")
    void invalidContentIsRejected() {
        assertThatThrownBy(() -> service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "   ")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.sendFromCustomer(CUSTOMER_ID, STORE_ID, null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "x".repeat(1001)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("1000");

        verify(messageRepository, never()).save(any());
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("Gửi quá 20 tin trong một phút bị chặn")
    void rateLimitBlocksFlooding() {
        when(messageRepository.countBySenderIdAndCreatedAtAfter(eq(CUSTOMER_ID), any(LocalDateTime.class))).thenReturn(20L);

        assertThatThrownBy(() -> service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "spam"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("quá nhanh");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chi nhánh không tồn tại hoặc đã ngừng hoạt động thì không bắt đầu chat được")
    void unknownOrInactiveStore() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "Alo")).isInstanceOf(BusinessException.class);

        store.setActive(false);
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        assertThatThrownBy(() -> service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "Alo")).isInstanceOf(BusinessException.class);

        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nhắn trong cuộc trò chuyện của người khác bị coi như không tồn tại")
    void cannotSendIntoSomeoneElsesConversation() {
        conversation(ChatStatus.OPEN, 0, 0);

        assertThatThrownBy(() -> service.sendInConversation(999L, CONVERSATION_ID, "Xin chào"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nhắn trong cuộc trò chuyện của chính mình đi cùng đường với tin gửi theo chi nhánh")
    void sendInOwnConversation() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 0);
        when(conversationRepository.findIdByCustomerAndStore(CUSTOMER_ID, STORE_ID)).thenReturn(Optional.of(CONVERSATION_ID));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

        ChatMessageDTO result = service.sendInConversation(CUSTOMER_ID, CONVERSATION_ID, "Còn không ạ");

        assertThat(result.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(conversation.getStaffUnread()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tin dài được cắt gọn thành bản xem trước 100 ký tự và gộp xuống dòng")
    void previewIsTruncated() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 0);
        when(conversationRepository.findIdByCustomerAndStore(CUSTOMER_ID, STORE_ID)).thenReturn(Optional.of(CONVERSATION_ID));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

        service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "dòng một\n\ndòng hai " + "x".repeat(200));

        assertThat(conversation.getLastMessagePreview()).hasSize(100).startsWith("dòng một dòng hai ").endsWith("…");
    }

    @Test
    @DisplayName("Lỗi WebSocket không làm mất tin đã lưu")
    void pushFailureIsSwallowed() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 0);
        when(conversationRepository.findIdByCustomerAndStore(CUSTOMER_ID, STORE_ID)).thenReturn(Optional.of(CONVERSATION_ID));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        doThrow(new IllegalStateException("broker down")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        ChatMessageDTO result = service.sendFromCustomer(CUSTOMER_ID, STORE_ID, "Alo");

        assertThat(result.getId()).isEqualTo(900L);
        verify(messageRepository).save(any(ChatMessage.class));
    }

    // ------------------------------------------------------------- nhân viên trả lời

    @Test
    @DisplayName("Nhân viên trả lời: khách nhận khung ở kênh riêng, chuông của khách báo một lần, số chưa đọc của nhân viên về 0")
    void staffReply() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 3);

        ChatMessageDTO result = service.sendFromStaff(STAFF_ID, CONVERSATION_ID, STORE_ID, "Dạ còn hàng ạ");

        assertThat(result.isFromCustomer()).isFalse();
        assertThat(result.getSenderName()).isEqualTo("Nhân viên OldBook Ha Noi");
        assertThat(conversation.getCustomerUnread()).isEqualTo(1);
        assertThat(conversation.getStaffUnread()).isZero();

        ArgumentCaptor<ChatEventDTO> event = ArgumentCaptor.forClass(ChatEventDTO.class);
        verify(messagingTemplate).convertAndSendToUser(eq("customer7"), eq("/queue/chat"), event.capture());
        assertThat(event.getValue().getConversation().getUnread()).as("số chưa đọc tính cho khách").isEqualTo(1);
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/store/1"), any(Object.class));
        verify(userNotificationService).notify(eq(CUSTOMER_ID), eq(UserNotificationType.CHAT),
                contains("OldBook Ha Noi"), eq("Dạ còn hàng ạ"), eq("/chat?conversation=" + CONVERSATION_ID));
    }

    @Test
    @DisplayName("Khách còn tin chưa đọc thì trả lời tiếp không tạo thêm thông báo trên chuông")
    void secondStaffReplyDoesNotNotifyAgain() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 1, 0);

        service.sendFromStaff(STAFF_ID, CONVERSATION_ID, STORE_ID, "Bạn cần thêm gì không?");

        assertThat(conversation.getCustomerUnread()).isEqualTo(2);
        verifyNoInteractions(userNotificationService);
    }

    @Test
    @DisplayName("Nhân viên chi nhánh khác coi như không thấy cuộc trò chuyện; quản lý/admin (scope null) thì xử lý được")
    void staffScope() {
        conversation(ChatStatus.OPEN, 0, 1);

        assertThatThrownBy(() -> service.sendFromStaff(STAFF_ID, CONVERSATION_ID, 2L, "Alo")).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.getStaffMessages(CONVERSATION_ID, 2L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.close(CONVERSATION_ID, 2L)).isInstanceOf(ResourceNotFoundException.class);
        verify(messageRepository, never()).save(any());

        assertThat(service.sendFromStaff(STAFF_ID, CONVERSATION_ID, null, "Quản lý trả lời").getContent()).isEqualTo("Quản lý trả lời");
    }

    @Test
    @DisplayName("Không trả lời được cuộc trò chuyện đã đóng")
    void cannotReplyToClosedConversation() {
        conversation(ChatStatus.CLOSED, 0, 0);

        assertThatThrownBy(() -> service.sendFromStaff(STAFF_ID, CONVERSATION_ID, STORE_ID, "Alo"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("đã đóng");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đóng cuộc trò chuyện: đổi trạng thái và báo cho khách cùng nhân viên chi nhánh, không kèm tin nhắn")
    void closePublishesStatusChange() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 0);

        ChatConversationDTO result = service.close(CONVERSATION_ID, STORE_ID);

        assertThat(conversation.getStatus()).isEqualTo(ChatStatus.CLOSED);
        assertThat(result.isClosed()).isTrue();
        ArgumentCaptor<ChatEventDTO> event = ArgumentCaptor.forClass(ChatEventDTO.class);
        verify(messagingTemplate).convertAndSendToUser(eq("customer7"), eq("/queue/chat"), event.capture());
        assertThat(event.getValue().getMessage()).isNull();
        assertThat(event.getValue().getConversation().isClosed()).isTrue();
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/store/1"), any(Object.class));
    }

    // ------------------------------------------------------------------- đọc tin

    @Test
    @DisplayName("Khách mở cuộc trò chuyện: nhận tin cũ trước, số chưa đọc về 0 và thông báo trên chuông được đánh dấu đã đọc")
    void customerReadsThread() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 2, 0);
        ChatMessage newer = ChatMessage.builder().conversation(conversation).sender(staff).fromCustomer(false).content("Dạ còn").build();
        newer.setId(2L);
        newer.setCreatedAt(LocalDateTime.of(2026, 10, 23, 9, 30));
        ChatMessage older = ChatMessage.builder().conversation(conversation).sender(customer).fromCustomer(true).content("Còn không?").build();
        older.setId(1L);
        older.setCreatedAt(LocalDateTime.of(2026, 10, 23, 9, 0));
        when(messageRepository.findLatest(eq(CONVERSATION_ID), any(Pageable.class))).thenReturn(List.of(newer, older));

        List<ChatMessageDTO> thread = service.getCustomerMessages(CUSTOMER_ID, CONVERSATION_ID);

        assertThat(thread).extracting(ChatMessageDTO::getContent).containsExactly("Còn không?", "Dạ còn");
        assertThat(conversation.getCustomerUnread()).isZero();
        verify(userNotificationService).markReadByLink(CUSTOMER_ID, "/chat?conversation=" + CONVERSATION_ID);
    }

    @Test
    @DisplayName("Khách không đọc được cuộc trò chuyện của người khác")
    void customerCannotReadOthersThread() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 1, 0);

        assertThatThrownBy(() -> service.getCustomerMessages(999L, CONVERSATION_ID)).isInstanceOf(ResourceNotFoundException.class);

        assertThat(conversation.getCustomerUnread()).isEqualTo(1);
        verify(messageRepository, never()).findLatest(anyLong(), any());
        verifyNoInteractions(userNotificationService);
    }

    @Test
    @DisplayName("Nhân viên mở cuộc trò chuyện: số chưa đọc của nhân viên về 0")
    void staffReadsThread() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 4);
        when(messageRepository.findLatest(eq(CONVERSATION_ID), any(Pageable.class))).thenReturn(List.of());

        service.getStaffMessages(CONVERSATION_ID, STORE_ID);

        assertThat(conversation.getStaffUnread()).isZero();
    }

    @Test
    @DisplayName("Hộp thư: nhân viên bị giới hạn ở chi nhánh của mình bất kể bộ lọc, quản lý/admin lọc tự do")
    void inboxScope() {
        ChatConversation conversation = conversation(ChatStatus.OPEN, 0, 1);
        when(conversationRepository.findForStaff(eq(STORE_ID), any(), any(Pageable.class))).thenReturn(List.of(conversation));
        when(conversationRepository.findForStaff(eq(null), eq(ChatStatus.OPEN), any(Pageable.class))).thenReturn(List.of(conversation));

        assertThat(service.getStaffConversations(STORE_ID, 3L, null)).hasSize(1);
        assertThat(service.getStaffConversations(null, null, ChatStatus.OPEN)).hasSize(1);

        verify(conversationRepository).findForStaff(eq(STORE_ID), eq(null), any(Pageable.class));
        verify(conversationRepository, never()).findForStaff(eq(3L), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Đếm cuộc trò chuyện chưa đọc cho cả hai phía")
    void unreadCounts() {
        when(conversationRepository.countWithStaffUnread(STORE_ID)).thenReturn(2L);
        when(conversationRepository.countWithCustomerUnread(CUSTOMER_ID)).thenReturn(1L);

        assertThat(service.countStaffUnread(STORE_ID)).isEqualTo(2);
        assertThat(service.countCustomerUnread(CUSTOMER_ID)).isEqualTo(1);
    }
}
