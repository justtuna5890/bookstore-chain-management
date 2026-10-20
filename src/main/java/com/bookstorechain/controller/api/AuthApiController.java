package com.bookstorechain.controller.api;

import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthApiController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            username = email != null ? email.split("@")[0] : "user_" + System.currentTimeMillis();
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + (System.currentTimeMillis() % 1000);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(body.get("password")));
        user.setFullName(body.get("fullName") != null ? body.get("fullName") : username);
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        User saved = userRepository.save(user);

        return Map.of(
                "message", "Đăng ký thành công",
                "userId", saved.getId(),
                "username", saved.getUsername(),
                "email", saved.getEmail()
        );
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(body.get("email"))
                .orElseThrow(() -> new RuntimeException("Sai email hoac mat khau"));

        if (!passwordEncoder.matches(body.get("password"), user.getPassword())) {
            throw new RuntimeException("Sai email hoac mat khau");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return Map.of("token", token);
    }
}
