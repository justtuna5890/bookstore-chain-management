package com.bookstorechain.security;

import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import com.bookstorechain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Security Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("loadUserByUsername - Tìm thấy theo username và ánh xạ đúng quyền ROLE_ADMIN")
    void loadUserByUsername_WhenFoundByUsername_ShouldReturnUserDetails() {
        User admin = User.builder()
                .username("admin")
                .email("admin@bookstore.com")
                .password("$2a$10$hashedPass")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$hashedPass");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("loadUserByUsername - Tìm thấy theo email khi username không khớp")
    void loadUserByUsername_WhenFoundByEmail_ShouldReturnUserDetails() {
        User staff = User.builder()
                .username("staff01")
                .email("staff@bookstore.com")
                .password("$2a$10$hashedPass")
                .role(Role.STAFF)
                .isActive(true)
                .build();

        when(userRepository.findByUsername("staff@bookstore.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("staff@bookstore.com")).thenReturn(Optional.of(staff));

        UserDetails userDetails = userDetailsService.loadUserByUsername("staff@bookstore.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("staff01");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_STAFF");
    }

    @Test
    @DisplayName("loadUserByUsername - Ném UsernameNotFoundException khi không tìm thấy tài khoản")
    void loadUserByUsername_WhenNotFound_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("notfound")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("notfound"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Không tìm thấy người dùng với username hoặc email: notfound");
    }
}
