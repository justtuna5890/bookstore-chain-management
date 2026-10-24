package com.bookstorechain.controller.api;

import com.bookstorechain.entity.User;
import com.bookstorechain.security.JwtUtil;
import com.bookstorechain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String username = body.get("username");
        String password = body.get("password");
        String fullName = body.get("fullName");
        String phone = body.get("phone");

        User saved = userService.registerCustomer(username, email, password, fullName, phone);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Đăng ký thành công",
                "userId", saved.getId(),
                "username", saved.getUsername(),
                "email", saved.getEmail()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String emailOrUsername = body.get("email") != null ? body.get("email") : body.get("username");
        String password = body.get("password");

        User user = userService.authenticate(emailOrUsername, password);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of("token", token));
    }
}
