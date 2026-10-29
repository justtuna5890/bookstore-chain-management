package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.BookCopyRequestDTO;
import com.bookstorechain.dto.response.BookCopyResponseDTO;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.BookCopyService;
import com.bookstorechain.service.BookService;
import com.bookstorechain.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/book-copies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class BookCopyController {

    private final BookCopyService bookCopyService;
    private final BookService bookService;
    private final StoreService storeService;

    @GetMapping
    public String listBookCopies(@RequestParam(value = "storeId", required = false) Long storeId,
                                 @RequestParam(value = "status", required = false) CopyStatus status,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());

        // Kiểm soát phạm vi chi nhánh nghiêm ngặt cho STAFF và MANAGER
        boolean isScoped = userDetails != null && (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER);
        if (isScoped) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                model.addAttribute("unassignedStoreWarning", true);
                model.addAttribute("copyPage", Page.empty());
                model.addAttribute("bookCopyPage", Page.empty());
                model.addAttribute("stores", List.of());
                model.addAttribute("statuses", CopyStatus.values());
                return "admin/inventory/copy-list";
            }
            storeId = staffStoreId;
            model.addAttribute("isStaffScoped", true);
            model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
            model.addAttribute("stores", List.of(userDetails.getUser().getStore()));
        } else {
            model.addAttribute("stores", storeService.getAllStores());
        }

        Page<BookCopyResponseDTO> copyPage = bookCopyService.getBookCopies(storeId, status, keyword, pageable);
        model.addAttribute("copyPage", copyPage);
        model.addAttribute("bookCopyPage", copyPage);
        model.addAttribute("statuses", CopyStatus.values());
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        return "admin/inventory/copy-list";
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        BookCopyRequestDTO form = new BookCopyRequestDTO();
        boolean isScoped = userDetails != null && (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER);

        if (isScoped) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản chưa được gán vào chi nhánh nào. Vui lòng liên hệ Quản trị viên để cấu hình chi nhánh!");
                return "redirect:/admin/book-copies";
            }
            form.setStoreId(staffStoreId);
            model.addAttribute("stores", List.of(userDetails.getUser().getStore()));
            model.addAttribute("isStaffScoped", true);
        } else {
            model.addAttribute("stores", storeService.getAllStores());
        }

        model.addAttribute("copyForm", form);
        model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
        model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
        return "admin/inventory/copy-form";
    }

    @PostMapping("/new")
    public String createBookCopy(@Valid @ModelAttribute("copyForm") BookCopyRequestDTO requestDTO,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        boolean isScoped = userDetails != null && (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER);
        Long staffStoreId = isScoped && userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;

        // Nếu là STAFF hoặc MANAGER, ép buộc gán đúng chi nhánh trực thuộc
        if (isScoped) {
            if (staffStoreId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản chưa được gán vào chi nhánh nào!");
                return "redirect:/admin/book-copies";
            }
            requestDTO.setStoreId(staffStoreId);
            model.addAttribute("stores", List.of(userDetails.getUser().getStore()));
            model.addAttribute("isStaffScoped", true);
        } else {
            model.addAttribute("stores", storeService.getAllStores());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
            return "admin/inventory/copy-form";
        }

        try {
            bookCopyService.createBookCopy(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm bản sách vào kho thành công!");
            return "redirect:/admin/book-copies";
        } catch (DuplicateResourceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
            return "admin/inventory/copy-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        BookCopyResponseDTO copy = bookCopyService.getBookCopyById(id);
        boolean isScoped = userDetails != null && (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER);

        if (isScoped) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null || !staffStoreId.equals(copy.getStoreId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên / Quản lý chỉ có quyền chỉnh sửa bản sách thuộc chi nhánh mình phụ trách!");
                return "redirect:/admin/book-copies";
            }
            model.addAttribute("stores", List.of(userDetails.getUser().getStore()));
            model.addAttribute("isStaffScoped", true);
        } else {
            model.addAttribute("stores", storeService.getAllStores());
        }

        BookCopyRequestDTO form = new BookCopyRequestDTO();
        form.setCopyCode(copy.getCopyCode());
        form.setBookId(copy.getBookId());
        form.setStoreId(copy.getStoreId());
        form.setBookCondition(copy.getBookCondition());
        form.setPrice(copy.getPrice());
        form.setNote(copy.getNote());

        model.addAttribute("copyId", id);
        model.addAttribute("copyForm", form);
        model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
        model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
        return "admin/inventory/copy-form";
    }

    @PostMapping("/{id}/edit")
    public String updateBookCopy(@PathVariable Long id,
                                 @Valid @ModelAttribute("copyForm") BookCopyRequestDTO requestDTO,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        boolean isScoped = userDetails != null && (userDetails.getUser().getRole() == Role.STAFF || userDetails.getUser().getRole() == Role.MANAGER);
        Long staffStoreId = isScoped && userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;

        // Nếu là STAFF hoặc MANAGER, ép buộc giữ nguyên chi nhánh trực thuộc của mình
        if (isScoped) {
            BookCopyResponseDTO currentCopy = bookCopyService.getBookCopyById(id);
            if (staffStoreId == null || !staffStoreId.equals(currentCopy.getStoreId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên / Quản lý chỉ có quyền chỉnh sửa bản sách thuộc chi nhánh mình phụ trách!");
                return "redirect:/admin/book-copies";
            }
            requestDTO.setStoreId(staffStoreId);
            model.addAttribute("stores", List.of(userDetails.getUser().getStore()));
            model.addAttribute("isStaffScoped", true);
        } else {
            model.addAttribute("stores", storeService.getAllStores());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("copyId", id);
            model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
            return "admin/inventory/copy-form";
        }

        try {
            bookCopyService.updateBookCopy(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bản sách thành công!");
            return "redirect:/admin/book-copies";
        } catch (DuplicateResourceException e) {
            model.addAttribute("copyId", id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
            return "admin/inventory/copy-form";
        }
    }
}
