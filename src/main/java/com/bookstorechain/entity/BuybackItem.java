package com.bookstorechain.entity;

import com.bookstorechain.enums.BookCondition;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "buyback_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuybackItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyback_request_id", nullable = false)
    private BuybackRequest buybackRequest;

    @Column(name = "book_title", nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String bookTitle; // Tên sách khách muốn bán

    @Column(name = "book_author", length = 100, columnDefinition = "NVARCHAR(100)")
    private String bookAuthor; // Tác giả sách

    @Enumerated(EnumType.STRING)
    @Column(name = "proposed_condition", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private BookCondition proposedCondition; // Tình trạng do nhân viên thẩm định

    @Column(name = "offered_price", precision = 12, scale = 2)
    private BigDecimal offeredPrice; // Giá cửa hàng đề xuất mua lại

    @Column(name = "item_image", columnDefinition = "NVARCHAR(500)")
    private String itemImage; // Ảnh thực tế cuốn sách khách gửi
}