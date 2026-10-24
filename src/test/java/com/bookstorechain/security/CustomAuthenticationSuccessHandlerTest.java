package com.bookstorechain.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomAuthenticationSuccessHandler Security Tests")
class CustomAuthenticationSuccessHandlerTest {

    private CustomAuthenticationSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new CustomAuthenticationSuccessHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("onAuthenticationSuccess - Điều hướng /admin/dashboard cho tài khoản ADMIN")
    void onAuthenticationSuccess_WhenAdmin_ShouldRedirectToAdminDashboard() throws IOException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).isEqualTo("/admin/dashboard");
    }

    @Test
    @DisplayName("onAuthenticationSuccess - Điều hướng /admin/dashboard cho tài khoản STAFF")
    void onAuthenticationSuccess_WhenStaff_ShouldRedirectToAdminDashboard() throws IOException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "staff", "password", List.of(new SimpleGrantedAuthority("ROLE_STAFF")));

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).isEqualTo("/admin/dashboard");
    }

    @Test
    @DisplayName("onAuthenticationSuccess - Điều hướng trang chủ '/' cho khách hàng CUSTOMER")
    void onAuthenticationSuccess_WhenCustomer_ShouldRedirectToHomePage() throws IOException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "customer", "password", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }
}
