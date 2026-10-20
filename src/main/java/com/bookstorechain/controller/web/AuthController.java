package com.bookstorechain.controller.web;

import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import com.bookstorechain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {

        // Nếu đã đăng nhập thì chuyển hướng thẳng vào dashboard
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/admin/dashboard";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công!");
        }

        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký thành công! Hãy đăng nhập bằng tài khoản mới.");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam("username") String username,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam("password") String password,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("errorMessage", "Tên đăng nhập '" + username + "' đã được sử dụng!");
            return "auth/register";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("errorMessage", "Địa chỉ email '" + email + "' đã tồn tại!");
            return "auth/register";
        }

        User user = User.builder()
                .username(username.trim())
                .fullName(fullName.trim())
                .email(email.trim().toLowerCase())
                .phone(phone != null ? phone.trim() : null)
                .password(passwordEncoder.encode(password))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login?registered=true";
    }
}