package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.BookCopyRequestDTO;
import com.bookstorechain.dto.response.BookCopyResponseDTO;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.service.BookCopyService;
import com.bookstorechain.service.BookService;
import com.bookstorechain.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
                                 Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        
        model.addAttribute("bookCopyPage", bookCopyService.getBookCopies(storeId, status, keyword, pageable));
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("statuses", CopyStatus.values());
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        return "admin/inventory/copy-list";
    }

    @GetMapping("/new")
    public String showCreateForm(@org.springframework.security.core.annotation.AuthenticationPrincipal com.bookstorechain.security.CustomUserDetails userDetails,
                                 Model model) {
        BookCopyRequestDTO form = new BookCopyRequestDTO();
        if (userDetails != null && userDetails.getUser().getRole() == com.bookstorechain.enums.Role.STAFF && userDetails.getUser().getStore() != null) {
            form.setStoreId(userDetails.getUser().getStore().getId());
        }
        model.addAttribute("copyForm", form);
        model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
        return "admin/inventory/copy-form";
    }

    @PostMapping("/new")
    public String createBookCopy(@Valid @ModelAttribute("copyForm") BookCopyRequestDTO requestDTO,
                                 BindingResult bindingResult,
                                 @org.springframework.security.core.annotation.AuthenticationPrincipal com.bookstorechain.security.CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        // Nếu là STAFF, ép buộc gán đúng chi nhánh trực thuộc
        if (userDetails != null && userDetails.getUser().getRole() == com.bookstorechain.enums.Role.STAFF) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                model.addAttribute("errorMessage", "Tài khoản nhân viên chưa được gán vào chi nhánh nào!");
                model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
                model.addAttribute("stores", storeService.getAllStores());
                model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
                return "admin/inventory/copy-form";
            }
            requestDTO.setStoreId(staffStoreId);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("stores", storeService.getAllStores());
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
            model.addAttribute("stores", storeService.getAllStores());
            model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
            return "admin/inventory/copy-form";
        }
    }
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        BookCopyResponseDTO copy = bookCopyService.getBookCopyById(id);

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
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
        return "admin/inventory/copy-form";
    }

    @PostMapping("/{id}/edit")
    public String updateBookCopy(@PathVariable Long id,
                                 @Valid @ModelAttribute("copyForm") BookCopyRequestDTO requestDTO,
                                 BindingResult bindingResult,
                                 @org.springframework.security.core.annotation.AuthenticationPrincipal com.bookstorechain.security.CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        // Nếu là STAFF, ép buộc giữ nguyên chi nhánh trực thuộc của mình
        if (userDetails != null && userDetails.getUser().getRole() == com.bookstorechain.enums.Role.STAFF) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            BookCopyResponseDTO currentCopy = bookCopyService.getBookCopyById(id);
            if (staffStoreId == null || !staffStoreId.equals(currentCopy.getStoreId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên chỉ có quyền chỉnh sửa bản sách thuộc chi nhánh mình!");
                return "redirect:/admin/book-copies";
            }
            requestDTO.setStoreId(staffStoreId);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("copyId", id);
            model.addAttribute("books", bookService.getBooks(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("stores", storeService.getAllStores());
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
            model.addAttribute("stores", storeService.getAllStores());
            model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
            return "admin/inventory/copy-form";
        }
    }
}