package com.bookstorechain.dto.response;

import com.bookstorechain.enums.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Một cuộc trò chuyện nhìn từ phía khách hoặc nhân viên; unread là số tin chưa đọc của phía đang xem. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversationDTO {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long customerId;
    private String customerName;
    private ChatStatus status;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private String timeAgo;
    private int unread;

    public boolean isClosed() {
        return status == ChatStatus.CLOSED;
    }
}
