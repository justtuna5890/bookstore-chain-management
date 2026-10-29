package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.PromotionRequestDTO;
import com.bookstorechain.enums.PromotionType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.service.AdminPromotionService;
import com.bookstorechain.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminPromotionController {

    private static final int PAGE_SIZE = 10;

    private final AdminPromotionService adminPromotionService;
    private final StoreService storeService;

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        model.addAttribute("promotionPage",
                adminPromotionService.list(PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("id").descending())));
        return "admin/promotion/promotion-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        LocalDateTime start = LocalDateTime.now().withSecond(0).withNano(0);
        PromotionRequestDTO form = PromotionRequestDTO.builder()
                .startsAt(start).endsAt(start.plusDays(30)).onePerCustomer(true).active(true).build();
        return form(model, form, null);
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") PromotionRequestDTO form, BindingResult bindingResult,
                         RedirectAttributes redirectAttributes, Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                String code = adminPromotionService.create(form).getCode();
                redirectAttributes.addFlashAttribute("successMessage", "Đã tạo mã giảm giá " + code + ".");
                return "redirect:/admin/promotions";
            } catch (BusinessException e) {
                model.addAttribute("formErrors", java.util.List.of(e.getMessage()));
            }
        } else {
            model.addAttribute("formErrors", messages(bindingResult));
        }
        return form(model, form, null);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            return form(model, adminPromotionService.toRequest(id), id);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") PromotionRequestDTO form,
                         BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        // Chuỗi mã không đổi được: luôn lấy lại từ bản ghi để form hiển thị đúng khi có lỗi
        try {
            form.setCode(adminPromotionService.get(id).getCode());
            if (!bindingResult.hasErrors()) {
                adminPromotionService.update(id, form);
                redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật mã giảm giá " + form.getCode() + ".");
                return "redirect:/admin/promotions";
            }
            model.addAttribute("formErrors", messages(bindingResult));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BusinessException e) {
            model.addAttribute("formErrors", java.util.List.of(e.getMessage()));
        }
        return form(model, form, id);
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean nowActive = !adminPromotionService.get(id).isActive();
            adminPromotionService.setActive(id, nowActive);
            redirectAttributes.addFlashAttribute("successMessage", nowActive ? "Đã kích hoạt mã giảm giá." : "Đã tạm dừng mã giảm giá.");
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        return "redirect:/admin/promotions";
    }

    private String form(Model model, PromotionRequestDTO form, Long editingId) {
        model.addAttribute("form", form);
        model.addAttribute("editingId", editingId);
        model.addAttribute("types", PromotionType.values());
        model.addAttribute("stores", storeService.getAllStores());
        return "admin/promotion/promotion-form";
    }

    private java.util.List<String> messages(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
    }
}
