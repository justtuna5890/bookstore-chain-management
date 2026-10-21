package com.bookstorechain.entity;

import com.bookstorechain.enums.Role;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50, columnDefinition = "NVARCHAR(50)")
    private String username;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "full_name", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String fullName;

    @Column(nullable = false, unique = true, length = 100, columnDefinition = "NVARCHAR(100)")
    private String email;

    @Column(length = 20, columnDefinition = "NVARCHAR(20)")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private Role role;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id")
    private Store store;
}