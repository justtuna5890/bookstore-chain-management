package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.BookCopyRequestDTO;
import com.bookstorechain.dto.response.BookCopyResponseDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.service.BookCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookCopyServiceImpl implements BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BookCopyResponseDTO> getBookCopies(Long storeId, CopyStatus status, String keyword, Pageable pageable) {
        String query = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return bookCopyRepository.searchBookCopies(storeId, status, query, pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public void createBookCopy(BookCopyRequestDTO requestDTO) {
        if (bookCopyRepository.existsByCopyCode(requestDTO.getCopyCode().trim())) {
            throw new DuplicateResourceException("Mã bản sách này đã tồn tại trong kho");
        }

        Book book = bookRepository.findById(requestDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đầu sách với ID: " + requestDTO.getBookId()));

        Store store = storeRepository.findById(requestDTO.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + requestDTO.getStoreId()));

        BookCopy bookCopy = BookCopy.builder()
                .copyCode(requestDTO.getCopyCode().trim())
                .book(book)
                .store(store)
                .bookCondition(requestDTO.getBookCondition())
                .price(requestDTO.getPrice())
                .note(requestDTO.getNote())
                .status(CopyStatus.AVAILABLE)
                .build();

        bookCopyRepository.save(bookCopy);
    }

    @Override
    @Transactional
    public void updateBookCopyStatus(Long id, CopyStatus status) {
        BookCopy bookCopy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản sách với ID: " + id));
        bookCopy.setStatus(status);
        bookCopyRepository.save(bookCopy);
    }

    private BookCopyResponseDTO mapToResponseDTO(BookCopy bc) {
        return BookCopyResponseDTO.builder()
                .id(bc.getId())
                .copyCode(bc.getCopyCode())
                .bookCondition(bc.getBookCondition())
                .price(bc.getPrice())
                .status(bc.getStatus())
                .note(bc.getNote())
                .bookId(bc.getBook().getId())
                .bookTitle(bc.getBook().getTitle())
                .bookAuthor(bc.getBook().getAuthor())
                .storeId(bc.getStore().getId())
                .storeName(bc.getStore().getName())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public BookCopyResponseDTO getBookCopyById(Long id) {
        BookCopy bc = bookCopyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản sách với ID: " + id));
        return mapToResponseDTO(bc);
    }

    @Override
    @Transactional
    public void updateBookCopy(Long id, BookCopyRequestDTO requestDTO) {
        BookCopy bookCopy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản sách với ID: " + id));

        String newCode = requestDTO.getCopyCode().trim();
        if (!bookCopy.getCopyCode().equalsIgnoreCase(newCode) && bookCopyRepository.existsByCopyCode(newCode)) {
            throw new DuplicateResourceException("Mã bản sách này đã tồn tại trong kho");
        }

        Book book = bookRepository.findById(requestDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đầu sách với ID: " + requestDTO.getBookId()));

        Store store = storeRepository.findById(requestDTO.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + requestDTO.getStoreId()));

        bookCopy.setCopyCode(newCode);
        bookCopy.setBook(book);
        bookCopy.setStore(store);
        bookCopy.setBookCondition(requestDTO.getBookCondition());
        bookCopy.setPrice(requestDTO.getPrice());
        bookCopy.setNote(requestDTO.getNote());

        bookCopyRepository.save(bookCopy);
    }
}