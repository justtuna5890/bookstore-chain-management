package com.bookstorechain.controller.api;

import com.bookstorechain.dto.request.BookRequestDTO;
import com.bookstorechain.dto.response.BookResponseDTO;
import com.bookstorechain.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookApiController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getAll(
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable) {
        Page<BookResponseDTO> books = bookService.getBooks(keyword, pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getById(@PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody BookRequestDTO requestDTO) {
        bookService.createBook(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Tạo đầu sách thành công!", "title", requestDTO.getTitle()));
    }
}
