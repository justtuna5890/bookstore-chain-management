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

/** Đánh giá của khách về một đầu sách; mỗi khách chỉ có một đánh giá cho mỗi đầu sách (sửa được). */
@Entity
@Table(name = "review", uniqueConstraints = @UniqueConstraint(name = "UQ_review_user_book", columnNames = {"user_id", "book_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private int rating; // 1-5 sao

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String comment;

    /** Quản trị ẩn đánh giá vi phạm; đánh giá ẩn không hiển thị và không tính vào điểm trung bình. */
    @Column(name = "is_hidden", nullable = false)
    @Builder.Default
    private boolean hidden = false;
}
