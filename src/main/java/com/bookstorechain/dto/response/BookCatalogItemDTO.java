package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCatalogItemDTO {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String category;
    private String description;
    private BigDecimal coverPrice;
    private String coverImage;
    private int availableCopiesCount;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> storeNames;
    private List<BookCopyResponseDTO> copies;
    /** Điểm đánh giá trung bình; có thể null ở những chỗ không cần hiển thị sao. */
    private RatingSummaryDTO rating;
}
