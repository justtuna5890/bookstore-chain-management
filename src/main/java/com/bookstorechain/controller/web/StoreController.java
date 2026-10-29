package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.StoreRequestDTO;
import com.bookstorechain.dto.response.StoreResponseDTO;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.Role;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/stores")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public String listStores(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null && userDetails.getUser().getRole() == Role.MANAGER) {
            Store managerStore = userDetails.getUser().getStore();
            if (managerStore != null) {
                model.addAttribute("stores", List.of(storeService.getStoreById(managerStore.getId())));
                model.addAttribute("isManagerScoped", true);
                model.addAttribute("currentStoreName", managerStore.getName());
            } else {
                model.addAttribute("stores", List.of());
                model.addAttribute("isManagerScoped", true);
            }
        } else {
            model.addAttribute("stores", storeService.getAllStores());
            model.addAttribute("isManagerScoped", false);
        }

        if (!model.containsAttribute("storeForm")) {
            model.addAttribute("storeForm", new StoreRequestDTO());
        }
        return "admin/store/store-list";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createStore(@Valid @ModelAttribute("storeForm") StoreRequestDTO requestDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("stores", storeService.getAllStores());
            model.addAttribute("openModal", true);
            return "admin/store/store-list";
        }

        try {
            storeService.createStore(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới chi nhánh thành công!");
        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/stores";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (userDetails != null && userDetails.getUser().getRole() == Role.MANAGER) {
            Store managerStore = userDetails.getUser().getStore();
            if (managerStore == null || !managerStore.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Quản lý chỉ có quyền xem và chỉnh sửa thông tin chi nhánh mình phụ trách!");
                return "redirect:/admin/stores";
            }
        }

        StoreResponseDTO store = storeService.getStoreById(id);
        
        StoreRequestDTO form = new StoreRequestDTO();
        form.setName(store.getName());
        form.setAddress(store.getAddress());
        form.setPhone(store.getPhone());

        model.addAttribute("storeId", id);
        model.addAttribute("storeForm", form);
        return "admin/store/store-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateStore(@PathVariable Long id,
                              @Valid @ModelAttribute("storeForm") StoreRequestDTO requestDTO,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (userDetails != null && userDetails.getUser().getRole() == Role.MANAGER) {
            Store managerStore = userDetails.getUser().getStore();
            if (managerStore == null || !managerStore.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Quản lý chỉ có quyền chỉnh sửa thông tin chi nhánh mình phụ trách!");
                return "redirect:/admin/stores";
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("storeId", id);
            return "admin/store/store-edit";
        }

        try {
            storeService.updateStore(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi nhánh thành công!");
            return "redirect:/admin/stores";
        } catch (DuplicateResourceException e) {
            model.addAttribute("storeId", id);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/store/store-edit";
        }
    }

    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            storeService.toggleStoreStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái chi nhánh thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/stores";
        }

        return "redirect:/admin/stores";
    }
}
