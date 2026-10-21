package com.bookstorechain.dto.response;

import com.bookstorechain.enums.BookCondition;
import com.bookstorechain.enums.CopyStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BookCopyResponseDTO {
    private Long id;
    private String copyCode;
    private BookCondition bookCondition;
    private BigDecimal price;
    private CopyStatus status;
    private String note;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private Long storeId;
    private String storeName;
}