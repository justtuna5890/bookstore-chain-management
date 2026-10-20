package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.BuybackResponseDTO;
import com.bookstorechain.entity.*;
import com.bookstorechain.enums.BookCondition;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.service.BuybackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuybackServiceImpl implements BuybackService {

    private final BuybackRequestRepository buybackRequestRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BuybackResponseDTO> getRequests(Long storeId, BuybackStatus status, Pageable pageable) {
        return buybackRequestRepository.searchRequests(storeId, status, pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public BuybackRequest getRequestById(Long id) {
        return buybackRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay yeu cau thu mua voi ID: " + id));
    }

    @Override
    @Transactional
    public void updateStatusAndPricing(Long requestId, BuybackStatus status, String staffNote,
                                        List<Long> itemIds, List<BigDecimal> offeredPrices) {
        BuybackRequest request = getRequestById(requestId);
        BuybackStatus currentStatus = request.getStatus();

        if (currentStatus == BuybackStatus.COMPLETED || currentStatus == BuybackStatus.REJECTED) {
            throw new IllegalStateException("Đơn thu mua đã " + currentStatus + ", không thể thay đổi trạng thái!");
        }

        request.setStatus(status);
        request.setStaffNote(staffNote);

        // Cap nhat gia de xuat cho tung item
        if (itemIds != null && offeredPrices != null) {
            for (int i = 0; i < itemIds.size(); i++) {
                Long itemId = itemIds.get(i);
                BigDecimal price = (i < offeredPrices.size()) ? offeredPrices.get(i) : null;
                for (BuybackItem item : request.getItems()) {
                    if (item.getId().equals(itemId)) {
                        item.setOfferedPrice(price);
                        break;
                    }
                }
            }
        }

        // Khi COMPLETED: tao BookCopy thuc su nhap kho
        if (status == BuybackStatus.COMPLETED) {
            createBookCopiesFromBuyback(request);
        }

        buybackRequestRepository.save(request);
    }

    /**
     * Tao BookCopy nhap kho tu cac BuybackItem da duoc dinh gia.
     * Tu dong tim hoac tao Book tuong ung theo title+author.
     */
    private void createBookCopiesFromBuyback(BuybackRequest request) {
        for (BuybackItem item : request.getItems()) {
            // Chi nhap kho nhung item da duoc dinh gia
            if (item.getOfferedPrice() == null || item.getOfferedPrice().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            String generatedCopyCode = "BB-" + request.getId() + "-" + item.getId();

            // Kiem tra tranh tao trung
            if (bookCopyRepository.existsByCopyCode(generatedCopyCode)) {
                log.info("BookCopy {} da ton tai, bo qua.", generatedCopyCode);
                continue;
            }

            // Tim hoac tao Book phu hop
            Book book = findOrCreateBook(item);

            // Xac dinh tinh trang sach (dung proposedCondition cua item, fallback FAIR)
            BookCondition condition = (item.getProposedCondition() != null)
                    ? item.getProposedCondition()
                    : BookCondition.FAIR;

            BookCopy bookCopy = BookCopy.builder()
                    .copyCode(generatedCopyCode)
                    .book(book)
                    .store(request.getStore())
                    .bookCondition(condition)
                    .price(item.getOfferedPrice())
                    .status(CopyStatus.AVAILABLE)
                    .note("Nhap kho tu thu mua #" + request.getId()
                          + " - Khach: " + request.getCustomer().getFullName())
                    .build();

            bookCopyRepository.save(bookCopy);
            log.info(">>> Da nhap kho BookCopy [{}] cho sach '{}' tu buyback #{}",
                    generatedCopyCode, book.getTitle(), request.getId());
        }
    }

    /**
     * Tim Book theo title + author. Neu chua co thi tu tao moi.
     */
    private Book findOrCreateBook(BuybackItem item) {
        String title = item.getBookTitle() != null ? item.getBookTitle().trim() : "Sach khong xac dinh";
        String author = item.getBookAuthor() != null ? item.getBookAuthor().trim() : "Tac gia khong xac dinh";

        // Tim sach trung title + author
        if (bookRepository.existsByTitleAndAuthor(title, author)) {
            return bookRepository.searchBooks(title, PageRequest.of(0, 10))
                    .getContent()
                    .stream()
                    .filter(b -> b.getAuthor().equalsIgnoreCase(author))
                    .findFirst()
                    .orElseGet(() -> createNewBook(title, author));
        }
        return createNewBook(title, author);
    }

    private Book createNewBook(String title, String author) {
        Book newBook = Book.builder()
                .title(title)
                .author(author)
                .publisher("Chua xac dinh")
                .description("Duoc tao tu dong tu quy trinh thu mua sach cu")
                .isActive(true)
                .build();
        Book saved = bookRepository.save(newBook);
        log.info(">>> Da tao Book moi: '{}' - '{}' (ID: {})", title, author, saved.getId());
        return saved;
    }

    private BuybackResponseDTO mapToResponseDTO(BuybackRequest br) {
        return BuybackResponseDTO.builder()
                .id(br.getId())
                .customerUsername(br.getCustomer().getUsername())
                .customerFullName(br.getCustomer().getFullName())
                .storeName(br.getStore().getName())
                .status(br.getStatus())
                .staffNote(br.getStaffNote())
                .itemCount(br.getItems() != null ? br.getItems().size() : 0)
                .createdAt(br.getCreatedAt())
                .build();
    }
}