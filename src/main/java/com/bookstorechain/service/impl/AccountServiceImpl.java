package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.PasswordChangeRequestDTO;
import com.bookstorechain.dto.request.ProfileRequestDTO;
import com.bookstorechain.dto.response.ProfileDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getProfile(Long userId) {
        return toDTO(findUser(userId));
    }

    @Override
    @Transactional
    public ProfileDTO updateProfile(Long userId, ProfileRequestDTO request) {
        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();
        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new BusinessException("Họ và tên không được để trống và tối đa 100 ký tự.");
        }
        User user = findUser(userId);
        user.setFullName(fullName);
        user.setPhone(request.getPhone() == null || request.getPhone().isBlank() ? null : request.getPhone().trim());
        return toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public String changePassword(Long userId, PasswordChangeRequestDTO request) {
        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH || newPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new BusinessException("Mật khẩu mới từ " + MIN_PASSWORD_LENGTH + " đến " + MAX_PASSWORD_LENGTH + " ký tự.");
        }
        if (!newPassword.equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu nhập lại không khớp.");
        }
        User user = findUser(userId);
        if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng.");
        }
        if (newPassword.equals(request.getCurrentPassword())) {
            throw new BusinessException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user).getPassword();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + userId));
    }

    private ProfileDTO toDTO(User user) {
        return ProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
