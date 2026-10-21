package com.bookstorechain.service;

import com.bookstorechain.dto.request.BookRequestDTO;
import com.bookstorechain.dto.response.BookResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    Page<BookResponseDTO> getBooks(String keyword, Pageable pageable);
    BookResponseDTO getBookById(Long id);
    void createBook(BookRequestDTO requestDTO);
    void updateBook(Long id, BookRequestDTO requestDTO);
    void toggleBookStatus(Long id);
}