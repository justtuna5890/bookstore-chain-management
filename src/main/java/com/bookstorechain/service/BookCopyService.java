package com.bookstorechain.service;

import com.bookstorechain.dto.request.BookCopyRequestDTO;
import com.bookstorechain.dto.response.BookCopyResponseDTO;
import com.bookstorechain.enums.CopyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookCopyService {
    Page<BookCopyResponseDTO> getBookCopies(Long storeId, CopyStatus status, String keyword, Pageable pageable);
    BookCopyResponseDTO getBookCopyById(Long id); // Bổ sung
    void createBookCopy(BookCopyRequestDTO requestDTO);
    void updateBookCopy(Long id, BookCopyRequestDTO requestDTO); // Bổ sung
    void updateBookCopyStatus(Long id, CopyStatus status);
}