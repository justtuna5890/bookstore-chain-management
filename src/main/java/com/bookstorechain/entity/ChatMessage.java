package com.bookstorechain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Một tin nhắn trong cuộc trò chuyện hỗ trợ. */
@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** true nếu do khách gửi, false nếu do nhân viên chi nhánh gửi. */
    @Column(name = "from_customer", nullable = false)
    private boolean fromCustomer;

    @Column(nullable = false, length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String content;
}
