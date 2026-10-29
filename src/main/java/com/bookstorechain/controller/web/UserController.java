package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.UserRequestDTO;
import com.bookstorechain.dto.response.UserResponseDTO;
import com.bookstorechain.enums.Role;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.service.StoreService;
import com.bookstorechain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final StoreService storeService;

    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "role", required = false) Role role,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserResponseDTO> userPage = userService.getUsers(keyword, role, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("roles", Role.values());
        model.addAttribute("stores", storeService.getAllStores());

        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserRequestDTO());
        }
        return "admin/user/user-list";
    }

    @PostMapping("/new")
    public String createUser(@Valid @ModelAttribute("userForm") UserRequestDTO requestDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("stores", storeService.getAllStores());
            model.addAttribute("openModal", true);
            // Reload user page
            Page<UserResponseDTO> userPage = userService.getUsers(null, null, PageRequest.of(0, 10, Sort.by("id").descending()));
            model.addAttribute("userPage", userPage);
            return "admin/user/user-list";
        }
        try {
            userService.createUser(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tài khoản thành công!");
        } catch (DuplicateResourceException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        UserResponseDTO user = userService.getUserById(id);

        UserRequestDTO form = new UserRequestDTO();
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRole(user.getRole());
        form.setStoreId(user.getStoreId());

        model.addAttribute("userId", id);
        model.addAttribute("userForm", form);
        model.addAttribute("roles", Role.values());
        model.addAttribute("stores", storeService.getAllStores());
        return "admin/user/user-form";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("userForm") UserRequestDTO requestDTO,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("stores", storeService.getAllStores());
            return "admin/user/user-form";
        }
        try {
            userService.updateUser(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
        } catch (DuplicateResourceException | IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("userId", id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("stores", storeService.getAllStores());
            return "admin/user/user-form";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái tài khoản!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/change-role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam("role") Role role,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đổi vai trò thành " + role + " thành công!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
