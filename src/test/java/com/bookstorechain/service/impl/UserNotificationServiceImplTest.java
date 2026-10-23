package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.UserNotificationDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.entity.UserNotification;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.repository.UserNotificationRepository;
import com.bookstorechain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceImplTest {

    private static final long USER_ID = 7L;
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-23T10:00:00Z"), ZoneId.of("UTC"));

    @Mock private UserNotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private UserNotificationServiceImpl service;
    private User customer;

    @BeforeEach
    void setUp() {
        service = new UserNotificationServiceImpl(notificationRepository, userRepository, messagingTemplate, CLOCK);
        customer = TestData.customer(USER_ID);
    }

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private void saveReturnsArgument() {
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(notificationRepository.save(any(UserNotification.class))).thenAnswer(inv -> {
            UserNotification n = inv.getArgument(0);
            n.setId(55L);
            n.setCreatedAt(LocalDateTime.of(2026, 10, 23, 9, 58));
            return n;
        });
    }

    @Test
    @DisplayName("Không trong transaction: lưu rồi đẩy ngay tới đúng người nhận qua kênh riêng")
    void notifyStoresAndPushesImmediatelyWithoutTransaction() {
        saveReturnsArgument();

        service.notify(USER_ID, UserNotificationType.ORDER, "Đơn đã xác nhận", "Đơn OB-1 đã được xác nhận", "/orders/OB-1");

        ArgumentCaptor<UserNotification> saved = ArgumentCaptor.forClass(UserNotification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(customer);
        assertThat(saved.getValue().getType()).isEqualTo(UserNotificationType.ORDER);
        assertThat(saved.getValue().isRead()).isFalse();

        ArgumentCaptor<UserNotificationDTO> payload = ArgumentCaptor.forClass(UserNotificationDTO.class);
        verify(messagingTemplate).convertAndSendToUser(eq("customer7"), eq("/queue/notifications"), payload.capture());
        assertThat(payload.getValue().getId()).isEqualTo(55L);
        assertThat(payload.getValue().getLink()).isEqualTo("/orders/OB-1");
        assertThat(payload.getValue().getTimeAgo()).isEqualTo("2 phút trước");
    }

    @Test
    @DisplayName("Trong transaction: chỉ đẩy sau khi commit, huỷ transaction thì không đẩy")
    void pushWaitsForCommit() {
        saveReturnsArgument();
        TransactionSynchronizationManager.initSynchronization();

        service.notify(USER_ID, UserNotificationType.ORDER, "Tiêu đề", "Nội dung", null);

        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any(Object.class));
        List<TransactionSynchronization> callbacks = TransactionSynchronizationManager.getSynchronizations();
        assertThat(callbacks).hasSize(1);

        callbacks.get(0).afterCommit();

        verify(messagingTemplate).convertAndSendToUser(eq("customer7"), eq("/queue/notifications"), any(Object.class));
    }

    @Test
    @DisplayName("Rollback: không có afterCommit nên không có khung nào được gửi")
    void rollbackSendsNothing() {
        saveReturnsArgument();
        TransactionSynchronizationManager.initSynchronization();

        service.notify(USER_ID, UserNotificationType.ORDER, "Tiêu đề", "Nội dung", null);
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any(Object.class));
    }

    @Test
    @DisplayName("Lỗi WebSocket không làm hỏng nghiệp vụ gọi vào")
    void pushFailureIsSwallowed() {
        saveReturnsArgument();
        doThrow(new IllegalStateException("broker down")).when(messagingTemplate)
                .convertAndSendToUser(any(), any(), any(Object.class));

        service.notify(USER_ID, UserNotificationType.WISHLIST, "Có hàng", "Sách có hàng lại", "/books/1");

        verify(notificationRepository).save(any(UserNotification.class));
    }

    @Test
    @DisplayName("Liên kết ngoài hoặc dạng '//host' bị bỏ, đường dẫn nội bộ được giữ")
    void onlyInternalLinksAreKept() {
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(notificationRepository.save(any(UserNotification.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<UserNotification> saved = ArgumentCaptor.forClass(UserNotification.class);

        for (String link : new String[]{"https://evil.example", "//evil.example", "javascript:alert(1)", "/\\evil", null, "/orders/1"}) {
            service.notify(USER_ID, UserNotificationType.ORDER, "t", "m", link);
        }

        verify(notificationRepository, org.mockito.Mockito.times(6)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(UserNotification::getLink)
                .containsExactly(null, null, null, null, null, "/orders/1");
    }

    @Test
    @DisplayName("Tiêu đề và nội dung quá dài bị cắt để vừa cột")
    void longTextIsTruncated() {
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(notificationRepository.save(any(UserNotification.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<UserNotification> saved = ArgumentCaptor.forClass(UserNotification.class);

        service.notify(USER_ID, UserNotificationType.REVIEW, "T".repeat(300), "M".repeat(900), null);

        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getTitle()).hasSize(150).endsWith("…");
        assertThat(saved.getValue().getMessage()).hasSize(500).endsWith("…");
    }

    @Test
    @DisplayName("Thời gian tương đối: vừa xong, phút, giờ, ngày rồi đến ngày tháng")
    void timeAgo() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 23, 10, 0);

        assertThat(service.timeAgo(now.minusSeconds(20))).isEqualTo("Vừa xong");
        assertThat(service.timeAgo(now.minusMinutes(5))).isEqualTo("5 phút trước");
        assertThat(service.timeAgo(now.minusHours(3))).isEqualTo("3 giờ trước");
        assertThat(service.timeAgo(now.minusDays(2))).isEqualTo("2 ngày trước");
        assertThat(service.timeAgo(now.minusDays(30))).isEqualTo("23/09/2026");
        assertThat(service.timeAgo(null)).isEmpty();
    }

    @Test
    @DisplayName("Danh sách gần đây sắp xếp mới nhất trước và giới hạn số dòng")
    void recentIsNewestFirstAndLimited() {
        UserNotification n = UserNotification.builder().user(customer).type(UserNotificationType.ORDER).title("t").message("m").build();
        n.setCreatedAt(LocalDateTime.of(2026, 10, 23, 9, 0));
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(notificationRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(n)));

        List<UserNotificationDTO> result = service.getRecent(USER_ID, 8);

        verify(notificationRepository).findByUserId(eq(USER_ID), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(8);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTimeAgo()).isEqualTo("1 giờ trước");
    }

    @Test
    @DisplayName("Trang thông báo giữ số trang và kích thước, luôn sắp mới nhất trước")
    void pageKeepsPagingButForcesSort() {
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(notificationRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(Page.empty());

        service.getPage(USER_ID, PageRequest.of(2, 15));

        verify(notificationRepository).findByUserId(eq(USER_ID), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(15);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();
    }

    @Test
    @DisplayName("Đánh dấu đã đọc chỉ thành công với thông báo của chính người đó")
    void markReadIsScopedToOwner() {
        when(notificationRepository.markRead(10L, USER_ID)).thenReturn(1);
        when(notificationRepository.markRead(11L, USER_ID)).thenReturn(0);
        when(notificationRepository.markAllRead(USER_ID)).thenReturn(3);
        when(notificationRepository.countByUserIdAndReadFalse(USER_ID)).thenReturn(4L);

        assertThat(service.markRead(USER_ID, 10L)).isTrue();
        assertThat(service.markRead(USER_ID, 11L)).isFalse();
        assertThat(service.markAllRead(USER_ID)).isEqualTo(3);
        assertThat(service.countUnread(USER_ID)).isEqualTo(4);
    }
}
