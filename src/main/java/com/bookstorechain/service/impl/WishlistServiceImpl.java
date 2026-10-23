package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.BookCatalogItemDTO;
import com.bookstorechain.dto.response.RatingSummaryDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.WishlistItem;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.repository.WishlistItemRepository;
import com.bookstorechain.service.ReviewService;
import com.bookstorechain.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final UserLocks userLocks;
    private final PlatformTransactionManager transactionManager;

    @Override
    public boolean toggle(Long userId, Long bookId) {
        // Khoá theo khách ngoài transaction: bấm tim hai lần liên tiếp không vi phạm ràng buộc duy nhất (user, book)
        synchronized (userLocks.lockFor(userId)) {
            return Boolean.TRUE.equals(new TransactionTemplate(transactionManager).execute(status -> doToggle(userId, bookId)));
        }
    }

    private boolean doToggle(Long userId, Long bookId) {
        Optional<WishlistItem> existing = wishlistItemRepository.findByUserIdAndBookId(userId, bookId);
        if (existing.isPresent()) {
            wishlistItemRepository.delete(existing.get());
            return false;
        }
        Book book = bookRepository.findById(bookId)
                .filter(Book::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đầu sách với ID: " + bookId));
        wishlistItemRepository.save(WishlistItem.builder().user(userRepository.getReferenceById(userId)).book(book).build());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getBookIds(Long userId) {
        return new HashSet<>(wishlistItemRepository.findBookIdsByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCatalogItemDTO> getItems(Long userId) {
        List<Book> books = wishlistItemRepository.findByUserIdWithBook(userId).stream()
                .map(WishlistItem::getBook).filter(Book::isActive).collect(Collectors.toList());
        Map<Long, RatingSummaryDTO> ratings = reviewService.getSummaries(books.stream().map(Book::getId).collect(Collectors.toList()));
        return books.stream().map(book -> {
            List<BookCopy> copies = bookCopyRepository.findByBookIdAndStatus(book.getId(), CopyStatus.AVAILABLE);
            BigDecimal minPrice = copies.stream().map(BookCopy::getPrice).filter(Objects::nonNull)
                    .reduce(BigDecimal::min).orElse(book.getCoverPrice());
            return BookCatalogItemDTO.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .category(book.getCategory())
                    .coverImage(book.getCoverImage())
                    .availableCopiesCount(copies.size())
                    .minPrice(minPrice)
                    .rating(ratings.get(book.getId()))
                    .build();
        }).collect(Collectors.toList());
    }
}
