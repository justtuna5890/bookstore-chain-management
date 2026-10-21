package com.bookstorechain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookResponseDTO {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String category;
    private String description;
    private BigDecimal coverPrice;
    private String coverImage;
    private boolean isActive;
    private LocalDateTime createdAt;
}