package com.bookstorechain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Đầu sách khách đánh dấu yêu thích; mỗi cặp (khách, đầu sách) chỉ có một dòng. */
@Entity
@Table(name = "wishlist_item", uniqueConstraints = @UniqueConstraint(name = "UQ_wishlist_user_book", columnNames = {"user_id", "book_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * Đã báo "có hàng lại" cho lần có hàng hiện tại. Đặt true ngay khi thêm nếu sách đang có hàng (không báo điều khách
     * đã thấy); trở về false khi sách hết hàng để lần có hàng sau được báo lại.
     */
    @Column(name = "in_stock_notified", nullable = false)
    @Builder.Default
    private boolean inStockNotified = false;
}
