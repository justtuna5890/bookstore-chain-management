package com.bookstorechain.entity;

import com.bookstorechain.enums.BookCondition;
import com.bookstorechain.enums.CopyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "book_copy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCopy extends BaseEntity {

    @Column(name = "copy_code", nullable = false, unique = true, length = 50, columnDefinition = "NVARCHAR(50)")
    private String copyCode; // Mã định danh từng cuốn sách cụ thể (VD: BC-HANOI-001)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private BookCondition bookCondition;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // Giá bán thực tế của cuốn sách này

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    @Builder.Default
    private CopyStatus status = CopyStatus.AVAILABLE;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note; // Ghi chú thêm về tình trạng (VD: Thiếu trang phụ bìa)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book; // Thuộc đầu sách nào

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // Thuộc chi nhánh nào quản lý kho
}