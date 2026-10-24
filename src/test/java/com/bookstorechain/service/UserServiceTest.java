package com.bookstorechain.service;

import com.bookstorechain.dto.request.UserRequestDTO;
import com.bookstorechain.dto.response.UserResponseDTO;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private Store sampleStore;

    @BeforeEach
    void setUp() {
        sampleStore = Store.builder().name("Chi nhánh Ba Đình").build();
        sampleStore.setId(1L);

        sampleUser = User.builder()
                .username("nhanvien1")
                .fullName("Nguyễn Văn Nhân Viên")
                .email("nhanvien1@bookstore.com")
                .password("$2a$10$encodedOldPassword")
                .role(Role.STAFF)
                .store(sampleStore)
                .isActive(true)
                .build();
        sampleUser.setId(10L);
    }

    @Test
    @DisplayName("getUserById - Thành công khi tồn tại ID")
    void getUserById_WhenExists_ShouldReturnUserResponseDTO() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        UserResponseDTO result = userService.getUserById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("nhanvien1");
        assertThat(result.getRole()).isEqualTo(Role.STAFF);
        assertThat(result.getStoreName()).isEqualTo("Chi nhánh Ba Đình");
    }

    @Test
    @DisplayName("getUserById - Bắt lỗi khi không tìm thấy tài khoản")
    void getUserById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy tài khoản với ID: 999");
    }

    @Test
    @DisplayName("createUser - Bắt lỗi khi tên đăng nhập đã tồn tại")
    void createUser_WhenUsernameExists_ShouldThrowDuplicateResourceException() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("nhanvien1");
        dto.setEmail("new@bookstore.com");

        when(userRepository.existsByUsername("nhanvien1")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Tên đăng nhập 'nhanvien1' đã tồn tại");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser - Bắt lỗi khi email đã được sử dụng")
    void createUser_WhenEmailExists_ShouldThrowDuplicateResourceException() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("nhanvien_moi");
        dto.setEmail("nhanvien1@bookstore.com");

        when(userRepository.existsByUsername("nhanvien_moi")).thenReturn(false);
        when(userRepository.existsByEmail("nhanvien1@bookstore.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email 'nhanvien1@bookstore.com' đã được sử dụng");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser - Tạo người dùng thành công: mã hóa mật khẩu BCrypt và lưu thông tin")
    void createUser_WhenValid_ShouldHashPasswordAndSave() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("staff_new");
        dto.setFullName("Lê Thị Thảo");
        dto.setEmail("thao@bookstore.com");
        dto.setPassword("plainText123");
        dto.setRole(Role.STAFF);
        dto.setStoreId(1L);

        when(userRepository.existsByUsername("staff_new")).thenReturn(false);
        when(userRepository.existsByEmail("thao@bookstore.com")).thenReturn(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));
        when(passwordEncoder.encode("plainText123")).thenReturn("$2a$10$hashedNewPassword");

        userService.createUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("staff_new");
        assertThat(savedUser.getPassword()).isEqualTo("$2a$10$hashedNewPassword");
        assertThat(savedUser.getRole()).isEqualTo(Role.STAFF);
        assertThat(savedUser.getStore()).isEqualTo(sampleStore);
        assertThat(savedUser.isActive()).isTrue();
    }

    @Test
    @DisplayName("updateUser - Đổi mật khẩu mới khi có nhập password mới")
    void updateUser_WhenNewPasswordProvided_ShouldEncodeNewPassword() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Nguyễn Văn Nhân Viên");
        dto.setEmail("nhanvien1@bookstore.com");
        dto.setRole(Role.STAFF);
        dto.setPassword("newPass456");

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("newPass456")).thenReturn("$2a$10$updatedEncodedPassword");

        userService.updateUser(10L, dto);

        assertThat(sampleUser.getPassword()).isEqualTo("$2a$10$updatedEncodedPassword");
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("updateUser - Giữ nguyên mật khẩu cũ nếu trường password bỏ trống")
    void updateUser_WhenPasswordBlank_ShouldKeepOldPassword() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Nguyễn Văn Nhân Viên Sửa");
        dto.setEmail("nhanvien1@bookstore.com");
        dto.setRole(Role.STAFF);
        dto.setPassword("");

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        userService.updateUser(10L, dto);

        assertThat(sampleUser.getPassword()).isEqualTo("$2a$10$encodedOldPassword");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("toggleUserStatus - Đảo ngược cờ kích hoạt isActive của tài khoản")
    void toggleUserStatus_ShouldInvertActiveFlag() {
        sampleUser.setActive(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        userService.toggleUserStatus(10L);

        assertThat(sampleUser.isActive()).isFalse();
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("changeUserRole - Thay đổi quyền hạn tài khoản")
    void changeUserRole_ShouldUpdateRole() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        userService.changeUserRole(10L, Role.ADMIN);

        assertThat(sampleUser.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("authenticate - Đăng nhập thành công khi mật khẩu khớp")
    void authenticate_WhenValidCredentials_ShouldReturnUser() {
        when(userRepository.findByEmail("nhanvien1@bookstore.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("rawPass123", "$2a$10$encodedOldPassword")).thenReturn(true);

        User authenticated = userService.authenticate("nhanvien1@bookstore.com", "rawPass123");

        assertThat(authenticated).isNotNull();
        assertThat(authenticated.getUsername()).isEqualTo("nhanvien1");
    }

    @Test
    @DisplayName("authenticate - Bắt lỗi khi mật khẩu không khớp")
    void authenticate_WhenWrongPassword_ShouldThrowIllegalArgumentException() {
        when(userRepository.findByEmail("nhanvien1@bookstore.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPass", "$2a$10$encodedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate("nhanvien1@bookstore.com", "wrongPass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sai tài khoản hoặc mật khẩu!");
    }

    @Test
    @DisplayName("registerCustomer - Đăng ký khách hàng mới thành công với vai trò CUSTOMER")
    void registerCustomer_WhenValid_ShouldEncodePasswordAndAssignCustomerRole() {
        when(userRepository.existsByUsername("khachhang1")).thenReturn(false);
        when(userRepository.existsByEmail("khach@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("custPass123")).thenReturn("$2a$10$encodedCustomerPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User customer = userService.registerCustomer("khachhang1", "khach@gmail.com", "custPass123", "Nguyễn Khách", "0911222333");

        assertThat(customer).isNotNull();
        assertThat(customer.getUsername()).isEqualTo("khachhang1");
        assertThat(customer.getEmail()).isEqualTo("khach@gmail.com");
        assertThat(customer.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(customer.getPassword()).isEqualTo("$2a$10$encodedCustomerPass");
        assertThat(customer.isActive()).isTrue();
    }
}
