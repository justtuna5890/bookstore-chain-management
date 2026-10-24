package com.bookstorechain.dto.response;

import com.bookstorechain.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private Long storeId;
    private String storeName;
    private boolean isActive;
    private LocalDateTime createdAt;
}
