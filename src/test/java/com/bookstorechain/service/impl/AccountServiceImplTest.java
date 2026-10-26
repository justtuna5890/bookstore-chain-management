package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.PasswordChangeRequestDTO;
import com.bookstorechain.dto.request.ProfileRequestDTO;
import com.bookstorechain.dto.response.ProfileDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final long USER_ID = 7L;
    private static final String CURRENT = "matkhau-cu";

    @Mock private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private AccountServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(userRepository, passwordEncoder);
        user = TestData.customer(USER_ID);
        user.setEmail("khach7@example.com");
        user.setPhone("0901234567");
        user.setPassword(passwordEncoder.encode(CURRENT));
    }

    private void userExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private void saveReturnsArgument() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PasswordChangeRequestDTO password(String current, String next, String confirm) {
        return PasswordChangeRequestDTO.builder().currentPassword(current).newPassword(next).confirmPassword(confirm).build();
    }

    @Test
    @DisplayName("Xem hồ sơ trả về thông tin của đúng tài khoản")
    void getProfile() {
        userExists();

        ProfileDTO profile = service.getProfile(USER_ID);

        assertThat(profile.getUsername()).isEqualTo("customer7");
        assertThat(profile.getEmail()).isEqualTo("khach7@example.com");
        assertThat(profile.getPhone()).isEqualTo("0901234567");
    }

    @Test
    @DisplayName("Tài khoản không tồn tại báo không tìm thấy")
    void unknownUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(USER_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Sửa hồ sơ: cắt khoảng trắng, giữ nguyên tên đăng nhập, email và mật khẩu")
    void updateProfile() {
        userExists();
        saveReturnsArgument();
        String passwordBefore = user.getPassword();

        ProfileDTO result = service.updateProfile(USER_ID, ProfileRequestDTO.builder().fullName("  Nguyen Van A ").phone(" 0912345678 ").build());

        assertThat(result.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(result.getPhone()).isEqualTo("0912345678");
        assertThat(user.getUsername()).isEqualTo("customer7");
        assertThat(user.getEmail()).isEqualTo("khach7@example.com");
        assertThat(user.getPassword()).isEqualTo(passwordBefore);
    }

    @Test
    @DisplayName("Số điện thoại để trống thì xoá số cũ")
    void blankPhoneClearsIt() {
        userExists();
        saveReturnsArgument();

        assertThat(service.updateProfile(USER_ID, ProfileRequestDTO.builder().fullName("A").phone("  ").build()).getPhone()).isNull();
    }

    @Test
    @DisplayName("Họ tên trống hoặc quá dài bị từ chối kể cả khi gọi thẳng service")
    void updateProfileRejectsBadName() {
        assertThatThrownBy(() -> service.updateProfile(USER_ID, ProfileRequestDTO.builder().fullName("   ").build()))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.updateProfile(USER_ID, ProfileRequestDTO.builder().fullName("x".repeat(101)).build()))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi mật khẩu đúng: lưu mật khẩu mới đã băm và trả về để đồng bộ phiên")
    void changePassword() {
        userExists();
        saveReturnsArgument();

        String hash = service.changePassword(USER_ID, password(CURRENT, "matkhau-moi", "matkhau-moi"));

        assertThat(hash).isEqualTo(user.getPassword());
        assertThat(passwordEncoder.matches("matkhau-moi", hash)).isTrue();
        assertThat(passwordEncoder.matches(CURRENT, hash)).isFalse();
        assertThat(hash).isNotEqualTo("matkhau-moi");
    }

    @Test
    @DisplayName("Sai mật khẩu hiện tại thì không đổi")
    void wrongCurrentPassword() {
        userExists();
        String before = user.getPassword();

        assertThatThrownBy(() -> service.changePassword(USER_ID, password("sai-roi", "matkhau-moi", "matkhau-moi")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("hiện tại không đúng");

        assertThat(user.getPassword()).isEqualTo(before);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Mật khẩu nhập lại không khớp, quá ngắn, quá dài hoặc trùng mật khẩu cũ đều bị từ chối")
    void invalidNewPasswords() {
        assertThatThrownBy(() -> service.changePassword(USER_ID, password(CURRENT, "matkhau-moi", "khac-nua")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("không khớp");
        assertThatThrownBy(() -> service.changePassword(USER_ID, password(CURRENT, "12345", "12345")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.changePassword(USER_ID, password(CURRENT, "x".repeat(65), "x".repeat(65))))
                .isInstanceOf(BusinessException.class);

        userExists();
        assertThatThrownBy(() -> service.changePassword(USER_ID, password(CURRENT, CURRENT, CURRENT)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("khác mật khẩu hiện tại");

        verify(userRepository, never()).save(any());
    }
}
