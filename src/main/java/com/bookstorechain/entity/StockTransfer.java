package com.bookstorechain.entity;

import com.bookstorechain.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransfer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_store_id", nullable = false)
    private Store senderStore; // Chi nhánh gửi sách đi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_store_id", nullable = false)
    private Store receiverStore; // Chi nhánh nhận sách

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // Nhân viên tạo phiếu

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    @Builder.Default
    private TransferStatus status = TransferStatus.PENDING;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note; // Ghi chú điều chuyển

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StockTransferItem> items = new ArrayList<>();
}