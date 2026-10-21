package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.BookRequestDTO;
import com.bookstorechain.dto.response.BookResponseDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.service.BookService;
import com.bookstorechain.service.FileUploadService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> getBooks(String keyword, Pageable pageable) {
        String query = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return bookRepository.searchBooks(query, pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
        return mapToResponseDTO(book);
    }

 // Inject thêm FileUploadService
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public void createBook(BookRequestDTO requestDTO) {
        if (bookRepository.existsByTitleAndAuthor(requestDTO.getTitle().trim(), requestDTO.getAuthor().trim())) {
            throw new DuplicateResourceException("Đầu sách này của tác giả đã tồn tại trong hệ thống");
        }

        // Tải ảnh lên Cloudinary nếu có file đính kèm
        String imageUrl = null;
        if (requestDTO.getImageFile() != null && !requestDTO.getImageFile().isEmpty()) {
            imageUrl = fileUploadService.uploadFile(requestDTO.getImageFile(), "books");
        }

        Book book = Book.builder()
                .title(requestDTO.getTitle().trim())
                .author(requestDTO.getAuthor().trim())
                .publisher(requestDTO.getPublisher() != null ? requestDTO.getPublisher().trim() : null)
                .publicationYear(requestDTO.getPublicationYear())
                .category(requestDTO.getCategory() != null ? requestDTO.getCategory().trim() : null)
                .description(requestDTO.getDescription())
                .coverPrice(requestDTO.getCoverPrice())
                .coverImage(imageUrl)
                .isActive(true)
                .build();

        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void updateBook(Long id, BookRequestDTO requestDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));

        // Nếu người dùng chọn file ảnh mới thì upload và thay thế URL cũ
        if (requestDTO.getImageFile() != null && !requestDTO.getImageFile().isEmpty()) {
            String newImageUrl = fileUploadService.uploadFile(requestDTO.getImageFile(), "books");
            book.setCoverImage(newImageUrl);
        }

        book.setTitle(requestDTO.getTitle().trim());
        book.setAuthor(requestDTO.getAuthor().trim());
        book.setPublisher(requestDTO.getPublisher() != null ? requestDTO.getPublisher().trim() : null);
        book.setPublicationYear(requestDTO.getPublicationYear());
        book.setCategory(requestDTO.getCategory() != null ? requestDTO.getCategory().trim() : null);
        book.setDescription(requestDTO.getDescription());
        book.setCoverPrice(requestDTO.getCoverPrice());

        bookRepository.save(book);
    }
    @Override
    @Transactional
    public void toggleBookStatus(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
        book.setActive(!book.isActive());
        bookRepository.save(book);
    }

    private BookResponseDTO mapToResponseDTO(Book book) {
        return BookResponseDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .category(book.getCategory())
                .description(book.getDescription())
                .coverPrice(book.getCoverPrice())
                .coverImage(book.getCoverImage())
                .isActive(book.isActive())
                .createdAt(book.getCreatedAt())
                .build();
    }
}