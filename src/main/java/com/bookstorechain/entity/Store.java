package com.bookstorechain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> employees = new ArrayList<>();
}