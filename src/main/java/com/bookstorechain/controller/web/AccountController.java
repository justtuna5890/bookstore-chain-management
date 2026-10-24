package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.PasswordChangeRequestDTO;
import com.bookstorechain.dto.request.ProfileRequestDTO;
import com.bookstorechain.dto.response.ProfileDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public String view(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("profile", accountService.getProfile(userDetails.getUser().getId()));
        return "customer/account";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileRequestDTO form,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("profileError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/account";
        }
        try {
            ProfileDTO updated = accountService.updateProfile(userDetails.getUser().getId(), form);
            // Phiên đăng nhập giữ bản sao người dùng để hiển thị tên ở thanh trên và điền sẵn form
            User sessionUser = userDetails.getUser();
            sessionUser.setFullName(updated.getFullName());
            sessionUser.setPhone(updated.getPhone());
            redirectAttributes.addFlashAttribute("profileSuccess", "Đã cập nhật thông tin tài khoản.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("profileError", e.getMessage());
        }
        return "redirect:/account";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeRequestDTO form,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("passwordError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/account";
        }
        try {
            String newHash = accountService.changePassword(userDetails.getUser().getId(), form);
            userDetails.getUser().setPassword(newHash);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Đã đổi mật khẩu. Hãy dùng mật khẩu mới ở lần đăng nhập sau.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/account";
    }
}
