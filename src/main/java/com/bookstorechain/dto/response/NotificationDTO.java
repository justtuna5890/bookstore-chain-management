package com.bookstorechain.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private String id;
    private String title;
    private String message;
    private String type; // "BUYBACK" or "TRANSFER"
    private Long storeId;
    private String storeName;
    private String targetUrl;
    private LocalDateTime timestamp;
}
