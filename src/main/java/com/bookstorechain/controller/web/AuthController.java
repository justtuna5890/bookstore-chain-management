package com.bookstorechain.controller.web;

import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {

        // Nếu đã đăng nhập thì chuyển hướng phù hợp theo Role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean isStaffOrAdmin = auth.getAuthorities().stream().anyMatch(role ->
                    role.getAuthority().equals("ROLE_ADMIN")
                            || role.getAuthority().equals("ROLE_MANAGER")
                            || role.getAuthority().equals("ROLE_STAFF"));
            if (isStaffOrAdmin) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/";
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

        try {
            userService.registerCustomer(username, email, password, fullName, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login?registered=true";
        } catch (DuplicateResourceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi đăng ký: " + e.getMessage());
            return "auth/register";
        }
    }
}
