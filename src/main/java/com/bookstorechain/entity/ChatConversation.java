package com.bookstorechain.entity;

import com.bookstorechain.enums.ChatStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Cuộc trò chuyện hỗ trợ giữa một khách và một chi nhánh; mỗi cặp (khách, chi nhánh) chỉ có một cuộc, mở lại khi khách nhắn tiếp. */
@Entity
@Table(name = "chat_conversation", uniqueConstraints = @UniqueConstraint(name = "UQ_chat_customer_store", columnNames = {"customer_id", "store_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "NVARCHAR(10)")
    @Builder.Default
    private ChatStatus status = ChatStatus.OPEN;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 100, columnDefinition = "NVARCHAR(100)")
    private String lastMessagePreview;

    /** Số tin nhân viên gửi mà khách chưa đọc. */
    @Column(name = "customer_unread", nullable = false)
    private int customerUnread;

    /** Số tin khách gửi mà nhân viên chưa đọc. */
    @Column(name = "staff_unread", nullable = false)
    private int staffUnread;
}
