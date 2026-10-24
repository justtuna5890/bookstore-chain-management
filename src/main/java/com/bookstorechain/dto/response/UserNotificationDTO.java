package com.bookstorechain.dto.response;

import com.bookstorechain.enums.UserNotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Thông báo riêng của một người dùng; cũng là nội dung khung gửi qua /user/queue/notifications. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationDTO {
    private Long id;
    private UserNotificationType type;
    private String title;
    private String message;
    private String link;
    private boolean read;
    private LocalDateTime createdAt;
    /** Thời điểm tương đối so với lúc tạo DTO, VD "5 phút trước". */
    private String timeAgo;
}
