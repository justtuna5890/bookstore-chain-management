package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.StoreRequestDTO;
import com.bookstorechain.dto.response.StoreResponseDTO;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/stores")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public String listStores(Model model) {
        model.addAttribute("stores", storeService.getAllStores());
        if (!model.containsAttribute("storeForm")) {
            model.addAttribute("storeForm", new StoreRequestDTO());
        }
        return "admin/store/store-list";
    }

    @PostMapping
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
    public String showEditForm(@PathVariable Long id, Model model) {
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
                              Model model,
                              RedirectAttributes redirectAttributes) {
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
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        storeService.toggleStoreStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái chi nhánh thành công!");
        return "redirect:/admin/stores";
    }
}