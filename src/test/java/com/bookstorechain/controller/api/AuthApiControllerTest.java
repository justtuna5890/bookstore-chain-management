package com.bookstorechain.controller.api;

import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import com.bookstorechain.security.JwtUtil;
import com.bookstorechain.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApiController API Tests")
class AuthApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthApiController authApiController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authApiController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/auth/login - Đăng nhập đúng thông tin trả về 200 OK và JWT Token")
    void login_WhenValidCredentials_ShouldReturnJwtToken() throws Exception {
        User user = User.builder()
                .email("admin@bookstore.com")
                .role(Role.ADMIN)
                .build();

        when(userService.authenticate("admin@bookstore.com", "password123")).thenReturn(user);
        when(jwtUtil.generateToken("admin@bookstore.com", "ADMIN")).thenReturn("mocked.jwt.token");

        Map<String, String> requestBody = Map.of(
                "email", "admin@bookstore.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Đăng ký tài khoản thành công trả về 201 Created")
    void register_WhenValid_ShouldReturnCreated() throws Exception {
        User savedUser = User.builder()
                .username("newcustomer")
                .email("customer@gmail.com")
                .fullName("Khách Hàng Mới")
                .role(Role.CUSTOMER)
                .build();
        savedUser.setId(99L);

        when(userService.registerCustomer("newcustomer", "customer@gmail.com", "pass1234", "Khách Hàng Mới", "0912345678"))
                .thenReturn(savedUser);

        Map<String, String> requestBody = Map.of(
                "username", "newcustomer",
                "email", "customer@gmail.com",
                "password", "pass1234",
                "fullName", "Khách Hàng Mới",
                "phone", "0912345678"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Đăng ký thành công"))
                .andExpect(jsonPath("$.userId").value(99))
                .andExpect(jsonPath("$.username").value("newcustomer"))
                .andExpect(jsonPath("$.email").value("customer@gmail.com"));
    }
}
