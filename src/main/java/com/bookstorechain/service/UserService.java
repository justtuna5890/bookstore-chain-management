package com.bookstorechain.service;

import com.bookstorechain.dto.request.UserRequestDTO;
import com.bookstorechain.dto.response.UserResponseDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponseDTO> getUsers(String keyword, Role role, Pageable pageable);

    UserResponseDTO getUserById(Long id);

    void createUser(UserRequestDTO requestDTO);

    void updateUser(Long id, UserRequestDTO requestDTO);

    void toggleUserStatus(Long id);

    void changeUserRole(Long id, Role role);

    User registerCustomer(String username, String email, String password, String fullName, String phone);

    User authenticate(String emailOrUsername, String rawPassword);
}
