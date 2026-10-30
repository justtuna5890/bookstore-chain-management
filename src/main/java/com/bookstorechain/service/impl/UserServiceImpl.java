package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.UserRequestDTO;
import com.bookstorechain.dto.response.UserResponseDTO;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getUsers(String keyword, Role role, Pageable pageable) {
        String query = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return userRepository.searchUsers(query, role, pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        return mapToResponseDTO(user);
    }

    @Override
    @Transactional
    public void createUser(UserRequestDTO requestDTO) {
        if (userRepository.existsByUsername(requestDTO.getUsername().trim())) {
            throw new DuplicateResourceException("Tên đăng nhập '" + requestDTO.getUsername() + "' đã tồn tại trong hệ thống");
        }
        if (userRepository.existsByEmail(requestDTO.getEmail().trim())) {
            throw new DuplicateResourceException("Email '" + requestDTO.getEmail() + "' đã được sử dụng");
        }

        if (requiresStore(requestDTO.getRole()) && requestDTO.getStoreId() == null) {
            throw new IllegalArgumentException("Nhân viên (STAFF), Quản lý (MANAGER) và Shipper (SHIPPER) bắt buộc phải được gán vào một chi nhánh cụ thể.");
        }

        Store store = null;
        if (requestDTO.getStoreId() != null) {
            store = storeRepository.findById(requestDTO.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + requestDTO.getStoreId()));
        }

        User user = User.builder()
                .username(requestDTO.getUsername().trim())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .fullName(requestDTO.getFullName().trim())
                .email(requestDTO.getEmail().trim())
                .phone(requestDTO.getPhone())
                .role(requestDTO.getRole())
                .store(store)
                .isActive(true)
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));

        if (requiresStore(requestDTO.getRole()) && requestDTO.getStoreId() == null) {
            throw new IllegalArgumentException("Nhân viên (STAFF), Quản lý (MANAGER) và Shipper (SHIPPER) bắt buộc phải được gán vào một chi nhánh cụ thể.");
        }

        if (user.getRole() == Role.ADMIN && requestDTO.getRole() != Role.ADMIN) {
            String currentUsername = getCurrentAuthenticatedUsername();
            if (currentUsername != null && currentUsername.equalsIgnoreCase(user.getUsername())) {
                throw new IllegalStateException("Bạn không thể tự hạ quyền Quản trị viên (ADMIN) của chính mình.");
            }
            if (user.isActive() && userRepository.countActiveAdmins() <= 1) {
                throw new IllegalStateException("Không thể hạ quyền Quản trị viên (ADMIN) đang hoạt động cuối cùng của hệ thống.");
            }
        }

        user.setFullName(requestDTO.getFullName().trim());
        user.setEmail(requestDTO.getEmail().trim());
        user.setPhone(requestDTO.getPhone());
        user.setRole(requestDTO.getRole());

        if (requestDTO.getStoreId() != null) {
            Store store = storeRepository.findById(requestDTO.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh"));
            user.setStore(store);
        } else {
            user.setStore(null);
        }

        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        if (user.isActive() && user.getRole() == Role.ADMIN) {
            String currentUsername = getCurrentAuthenticatedUsername();
            if (currentUsername != null && currentUsername.equalsIgnoreCase(user.getUsername())) {
                throw new IllegalStateException("Bạn không thể tự khóa tài khoản Quản trị viên (ADMIN) của chính mình.");
            }
            if (userRepository.countActiveAdmins() <= 1) {
                throw new IllegalStateException("Không thể khóa tài khoản Quản trị viên (ADMIN) đang hoạt động cuối cùng của hệ thống.");
            }
        }

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        if (user.getRole() == Role.ADMIN && role != Role.ADMIN) {
            String currentUsername = getCurrentAuthenticatedUsername();
            if (currentUsername != null && currentUsername.equalsIgnoreCase(user.getUsername())) {
                throw new IllegalStateException("Bạn không thể tự hạ quyền Quản trị viên (ADMIN) của chính mình.");
            }
            if (user.isActive() && userRepository.countActiveAdmins() <= 1) {
                throw new IllegalStateException("Không thể hạ quyền Quản trị viên (ADMIN) đang hoạt động cuối cùng của hệ thống.");
            }
        }

        if (requiresStore(role) && user.getStore() == null) {
            throw new IllegalArgumentException("Không thể chuyển sang vai trò " + role + " vì tài khoản chưa được gán chi nhánh. Hãy chỉnh sửa thông tin tài khoản và chọn chi nhánh.");
        }

        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerCustomer(String username, String email, String password, String fullName, String phone) {
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        String cleanUsername = username != null ? username.trim() : "";
        if (cleanUsername.isEmpty()) {
            cleanUsername = cleanEmail.split("@")[0];
        }

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new DuplicateResourceException("Tên đăng nhập '" + cleanUsername + "' đã được sử dụng!");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new DuplicateResourceException("Địa chỉ email '" + cleanEmail + "' đã tồn tại!");
        }

        User user = User.builder()
                .username(cleanUsername)
                .fullName(fullName != null && !fullName.isBlank() ? fullName.trim() : cleanUsername)
                .email(cleanEmail)
                .phone(phone != null ? phone.trim() : null)
                .password(passwordEncoder.encode(password))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User authenticate(String emailOrUsername, String rawPassword) {
        User user = userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername))
                .orElseThrow(() -> new IllegalArgumentException("Sai tài khoản hoặc mật khẩu!"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Sai tài khoản hoặc mật khẩu!");
        }
        return user;
    }

    /** Vai trò làm việc tại một chi nhánh cụ thể nên bắt buộc phải có chi nhánh. */
    private boolean requiresStore(Role role) {
        return role == Role.STAFF || role == Role.MANAGER || role == Role.SHIPPER;
    }

    private String getCurrentAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .storeId(user.getStore() != null ? user.getStore().getId() : null)
                .storeName(user.getStore() != null ? user.getStore().getName() : null)
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
