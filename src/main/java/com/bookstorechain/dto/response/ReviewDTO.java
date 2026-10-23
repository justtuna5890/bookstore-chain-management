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
public class ReviewDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String reviewerName;
    private int rating;
    private String comment;
    private boolean hidden;
    private LocalDateTime createdAt;

    public String getFormattedCreatedAt() {
        return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Phần trăm độ rộng lớp sao vàng khi vẽ 5 sao (rating/5). */
    public int getStarPercent() {
        return rating * 20;
    }
}
