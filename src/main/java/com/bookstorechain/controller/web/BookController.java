package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.BookRequestDTO;
import com.bookstorechain.dto.response.BookResponseDTO;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.service.BookService;
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
@RequestMapping("/admin/books")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'MANAGER')")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public String listBooks(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<BookResponseDTO> bookPage = bookService.getBooks(keyword, pageable);

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("keyword", keyword);
        return "admin/book/book-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String showCreateForm(Model model) {
        model.addAttribute("bookForm", new BookRequestDTO());
        return "admin/book/book-form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String createBook(@Valid @ModelAttribute("bookForm") BookRequestDTO requestDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/book/book-form";
        }

        try {
            bookService.createBook(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm đầu sách thành công!");
            return "redirect:/admin/books";
        } catch (DuplicateResourceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/book/book-form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        BookResponseDTO book = bookService.getBookById(id);

        BookRequestDTO form = new BookRequestDTO();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setPublisher(book.getPublisher());
        form.setPublicationYear(book.getPublicationYear());
        form.setCategory(book.getCategory());
        form.setDescription(book.getDescription());
        form.setCoverPrice(book.getCoverPrice());
        form.setCoverImage(book.getCoverImage());

        model.addAttribute("bookId", id);
        model.addAttribute("bookForm", form);
        return "admin/book/book-form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String updateBook(@PathVariable Long id,
                             @Valid @ModelAttribute("bookForm") BookRequestDTO requestDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", id);
            return "admin/book/book-form";
        }

        bookService.updateBook(id, requestDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin sách thành công!");
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.toggleBookStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái sách!");
        return "redirect:/admin/books";
    }
}
