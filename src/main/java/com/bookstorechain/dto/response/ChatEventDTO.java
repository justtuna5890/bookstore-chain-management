package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Khung realtime khi có tin mới: cuộc trò chuyện (số chưa đọc tính cho người nhận) và tin vừa gửi. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEventDTO {
    private ChatConversationDTO conversation;
    private ChatMessageDTO message;
}
