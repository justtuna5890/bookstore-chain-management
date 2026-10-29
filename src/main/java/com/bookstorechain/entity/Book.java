package com.bookstorechain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book extends BaseEntity {

    @Column(nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String title;

    @Column(nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String author;

    @Column(length = 100, columnDefinition = "NVARCHAR(100)")
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(length = 50, columnDefinition = "NVARCHAR(50)")
    private String category;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "cover_price", precision = 12, scale = 2)
    private BigDecimal coverPrice;

    @Column(name = "cover_image", columnDefinition = "NVARCHAR(500)")
    private String coverImage;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}