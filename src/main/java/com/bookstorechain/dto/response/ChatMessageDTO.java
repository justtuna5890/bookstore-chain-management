package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private boolean fromCustomer;
    /** Tên người gửi: tên khách, hoặc "Nhân viên {chi nhánh}" (không lộ tên nhân viên cho khách). */
    private String senderName;
    private String content;
    private LocalDateTime createdAt;

    public String getTimeLabel() {
        return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("HH:mm dd/MM"));
    }
}
