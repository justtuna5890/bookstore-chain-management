package com.bookstorechain.dto.response;

import com.bookstorechain.enums.BuybackStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BuybackResponseDTO {
    private Long id;
    private String customerUsername;
    private String customerFullName;
    private String storeName;
    private BuybackStatus status;
    private String staffNote;
    private int itemCount;
    private LocalDateTime createdAt;
}