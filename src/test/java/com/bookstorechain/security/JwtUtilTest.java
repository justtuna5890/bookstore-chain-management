package com.bookstorechain.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String secretKey = "super_secret_jwt_key_that_is_at_least_256_bits_long_for_hmac_sha256";
    private final long expirationMs = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secretKey);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", expirationMs);
    }

    @Test
    @DisplayName("generateToken & extractEmail - Sinh token và trích xuất đúng email")
    void generateTokenAndExtractEmail_ShouldSucceed() {
        String email = "admin@bookstore.com";
        String role = "ROLE_ADMIN";

        String token = jwtUtil.generateToken(email, role);

        assertThat(token).isNotNull().isNotEmpty();
        String extractedEmail = jwtUtil.extractEmail(token);
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("isTokenValid - Trả về true khi email trùng khớp và token chưa hết hạn")
    void isTokenValid_WhenEmailMatches_ShouldReturnTrue() {
        String email = "staff@bookstore.com";
        String token = jwtUtil.generateToken(email, "ROLE_STAFF");

        boolean isValid = jwtUtil.isTokenValid(token, email);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid - Trả về false khi email không trùng khớp")
    void isTokenValid_WhenEmailMismatches_ShouldReturnFalse() {
        String email = "staff@bookstore.com";
        String token = jwtUtil.generateToken(email, "ROLE_STAFF");

        boolean isValid = jwtUtil.isTokenValid(token, "other@bookstore.com");

        assertThat(isValid).isFalse();
    }
}
