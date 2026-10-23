package com.bookstorechain.entity;

import com.bookstorechain.enums.UserNotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Thông báo gửi riêng cho một người dùng; được lưu lại để xem sau khi người đó không trực tuyến. */
@Entity
@Table(name = "user_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private UserNotificationType type;

    @Column(nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String title;

    @Column(nullable = false, length = 500, columnDefinition = "NVARCHAR(500)")
    private String message;

    /** Đường dẫn nội bộ mở khi bấm vào thông báo; có thể để trống. */
    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String link;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;
}
