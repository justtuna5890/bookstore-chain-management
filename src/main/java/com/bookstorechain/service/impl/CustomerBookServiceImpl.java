package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.BookCatalogItemDTO;
import com.bookstorechain.dto.response.BookCopyResponseDTO;
import com.bookstorechain.dto.response.BookDetailDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.service.CustomerBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerBookServiceImpl implements CustomerBookService {

    private static final int RELATED_BOOKS_LIMIT = 4;

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getBookDetail(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .filter(Book::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đầu sách với ID: " + bookId));

        List<BookCopy> copies = availableCopiesOf(book.getId());

        return BookDetailDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .category(book.getCategory())
                .description(book.getDescription())
                .coverPrice(book.getCoverPrice())
                .coverImage(book.getCoverImage())
                .availableCopiesCount(copies.size())
                .storeCount((int) copies.stream().map(c -> c.getStore().getId()).distinct().count())
                .minPrice(priceBound(copies, true, book.getCoverPrice()))
                .maxPrice(priceBound(copies, false, book.getCoverPrice()))
                .copies(copies.stream().map(this::toCopyDTO).collect(Collectors.toList()))
                .relatedBooks(findRelatedBooks(book))
                .build();
    }

    private List<BookCopy> availableCopiesOf(Long bookId) {
        return bookCopyRepository.findByBookIdAndStatus(bookId, CopyStatus.AVAILABLE).stream()
                .sorted(Comparator.comparing(BookCopy::getPrice))
                .collect(Collectors.toList());
    }

    private List<BookCatalogItemDTO> findRelatedBooks(Book book) {
        if (book.getCategory() == null || book.getCategory().isBlank()) {
            return Collections.emptyList();
        }
        return bookRepository.findTop4ByCategoryAndIdNotAndIsActiveTrue(book.getCategory(), book.getId()).stream()
                .limit(RELATED_BOOKS_LIMIT)
                .map(related -> {
                    List<BookCopy> copies = availableCopiesOf(related.getId());
                    return BookCatalogItemDTO.builder()
                            .id(related.getId())
                            .title(related.getTitle())
                            .author(related.getAuthor())
                            .category(related.getCategory())
                            .coverImage(related.getCoverImage())
                            .availableCopiesCount(copies.size())
                            .minPrice(priceBound(copies, true, related.getCoverPrice()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal priceBound(List<BookCopy> copies, boolean min, BigDecimal fallback) {
        return copies.stream()
                .map(BookCopy::getPrice)
                .filter(Objects::nonNull)
                .reduce(min ? BigDecimal::min : BigDecimal::max)
                .orElse(fallback);
    }

    private BookCopyResponseDTO toCopyDTO(BookCopy copy) {
        return BookCopyResponseDTO.builder()
                .id(copy.getId())
                .copyCode(copy.getCopyCode())
                .bookId(copy.getBook().getId())
                .bookTitle(copy.getBook().getTitle())
                .storeId(copy.getStore().getId())
                .storeName(copy.getStore().getName())
                .bookCondition(copy.getBookCondition())
                .status(copy.getStatus())
                .price(copy.getPrice())
                .note(copy.getNote())
                .build();
    }
}
