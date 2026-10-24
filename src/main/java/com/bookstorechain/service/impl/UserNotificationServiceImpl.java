package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.UserNotificationDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.entity.UserNotification;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.repository.UserNotificationRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserNotificationServiceImpl implements UserNotificationService {

    /** Đích gửi cho từng người: Spring tự đổi thành /user/{tên đăng nhập}/queue/notifications. */
    static final String USER_QUEUE = "/queue/notifications";

    private static final int MAX_TITLE = 150;
    private static final int MAX_MESSAGE = 500;
    private static final int MAX_LINK = 255;

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    @Override
    @Transactional
    public void notify(Long userId, UserNotificationType type, String title, String message, String link) {
        User user = userRepository.getReferenceById(userId);
        UserNotification saved = notificationRepository.save(UserNotification.builder()
                .user(user)
                .type(type)
                .title(truncate(title, MAX_TITLE))
                .message(truncate(message, MAX_MESSAGE))
                .link(internalLink(link))
                .build());

        UserNotificationDTO payload = toDTO(saved);
        String username = user.getUsername();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    push(username, payload);
                }
            });
        } else {
            push(username, payload);
        }
    }

    private void push(String username, UserNotificationDTO payload) {
        try {
            messagingTemplate.convertAndSendToUser(username, USER_QUEUE, payload);
        } catch (Exception e) {
            log.error("Lỗi khi đẩy thông báo WebSocket cho {}", username, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserNotificationDTO> getRecent(Long userId, int limit) {
        return notificationRepository.findByUserId(userId, newestFirst(0, limit)).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserNotificationDTO> getPage(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newestFirstSort())).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public boolean markRead(Long userId, Long notificationId) {
        return notificationRepository.markRead(notificationId, userId) > 0;
    }

    @Override
    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId);
    }

    private Pageable newestFirst(int page, int size) {
        return PageRequest.of(page, size, newestFirstSort());
    }

    private Sort newestFirstSort() {
        return Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private UserNotificationDTO toDTO(UserNotification notification) {
        return UserNotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .timeAgo(timeAgo(notification.getCreatedAt()))
                .build();
    }

    String timeAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        Duration age = Duration.between(createdAt, LocalDateTime.now(clock));
        if (age.toMinutes() < 1) {
            return "Vừa xong";
        }
        if (age.toMinutes() < 60) {
            return age.toMinutes() + " phút trước";
        }
        if (age.toHours() < 24) {
            return age.toHours() + " giờ trước";
        }
        if (age.toDays() < 7) {
            return age.toDays() + " ngày trước";
        }
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String truncate(String text, int max) {
        String value = text == null ? "" : text.trim();
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    /** Chỉ nhận đường dẫn nội bộ ("/orders/..."), không nhận địa chỉ ngoài hay "//host" để thông báo không dẫn khách đi nơi khác. */
    private String internalLink(String link) {
        if (link == null || link.length() > MAX_LINK || !link.startsWith("/") || link.startsWith("//") || link.contains("\\")) {
            return null;
        }
        return link;
    }
}
