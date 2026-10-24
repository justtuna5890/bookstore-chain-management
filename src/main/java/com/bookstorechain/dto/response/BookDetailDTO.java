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
public class BookDetailDTO {
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
    private int storeCount;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<BookCopyResponseDTO> copies;
    private List<BookCatalogItemDTO> relatedBooks;
}
