package com.bookstorechain.dto.response;

import com.bookstorechain.enums.TransferStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StockTransferResponseDTO {
    private Long id;
    private String senderStoreName;
    private String receiverStoreName;
    private String creatorUsername;
    private TransferStatus status;
    private String note;
    private int itemCount;
    private LocalDateTime createdAt;
}