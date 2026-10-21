package com.bookstorechain.entity;

import com.bookstorechain.enums.BuybackStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buyback_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuybackRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User customer; // Khách hàng gửi yêu cầu

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // Chi nhánh tiếp nhận xử lý

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    @Builder.Default
    private BuybackStatus status = BuybackStatus.PENDING;

    @Column(name = "staff_note", columnDefinition = "NVARCHAR(MAX)")
    private String staffNote; // Ghi chú của nhân viên kiểm tra

    @OneToMany(mappedBy = "buybackRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BuybackItem> items = new ArrayList<>();
}