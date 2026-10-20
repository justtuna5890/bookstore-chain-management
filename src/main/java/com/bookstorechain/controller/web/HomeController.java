package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.CustomerBuybackDTO;
import com.bookstorechain.dto.response.BookCatalogItemDTO;
import com.bookstorechain.dto.response.BookCopyResponseDTO;
import com.bookstorechain.entity.*;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.repository.*;
import com.bookstorechain.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final StoreRepository storeRepository;
    private final BuybackRequestRepository buybackRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping({"/", "/home"})
    public String home(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "storeId", required = false) Long storeId,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model) {
        populateHomeModel(model, keyword, category, storeId, userDetails, new CustomerBuybackDTO());
        return "index";
    }

    @PostMapping("/buyback-request")
    public String submitBuybackRequest(@Valid @ModelAttribute("buybackForm") CustomerBuybackDTO buybackForm,
                                       BindingResult bindingResult,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            populateHomeModel(model, null, null, null, userDetails, buybackForm);
            model.addAttribute("buybackError", "Vui lòng kiểm tra lại các trường thông tin thu mua!");
            return "index";
        }

        try {
            // Xác định hoặc tạo tài khoản khách hàng
            User customer;
            if (userDetails != null && userDetails.getUser() != null) {
                customer = userDetails.getUser();
            } else {
                String email = buybackForm.getCustomerEmail() != null ? buybackForm.getCustomerEmail().trim() : "";
                if (email.isEmpty()) {
                    populateHomeModel(model, null, null, null, userDetails, buybackForm);
                    model.addAttribute("buybackError", "Email không được để trống!");
                    return "index";
                }

                customer = userRepository.findByEmail(email)
                        .orElseGet(() -> {
                            User newUser = User.builder()
                                    .username(email)
                                    .email(email)
                                    .fullName(buybackForm.getCustomerName() != null && !buybackForm.getCustomerName().isBlank()
                                            ? buybackForm.getCustomerName() : "Khách Hàng " + email.split("@")[0])
                                    .phone(buybackForm.getCustomerPhone())
                                    .password(passwordEncoder.encode(UUID.randomUUID().toString().substring(0, 8)))
                                    .role(Role.CUSTOMER)
                                    .isActive(true)
                                    .build();
                            return userRepository.save(newUser);
                        });
            }

            Store store = storeRepository.findById(buybackForm.getStoreId())
                    .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không tồn tại!"));

            BuybackRequest request = BuybackRequest.builder()
                    .customer(customer)
                    .store(store)
                    .status(BuybackStatus.PENDING)
                    .staffNote("Yêu cầu gửi từ cổng khách hàng trực tuyến. Ghi chú: " +
                            (buybackForm.getNote() != null ? buybackForm.getNote() : "Không có"))
                    .build();

            BuybackItem item = BuybackItem.builder()
                    .buybackRequest(request)
                    .bookTitle(buybackForm.getBookTitle())
                    .bookAuthor(buybackForm.getBookAuthor() != null && !buybackForm.getBookAuthor().isBlank()
                            ? buybackForm.getBookAuthor() : "Chưa rõ")
                    .proposedCondition(buybackForm.getCondition())
                    .build();

            request.getItems().add(item);
            BuybackRequest saved = buybackRequestRepository.save(request);

            redirectAttributes.addFlashAttribute("buybackSuccessMsg",
                    "Gửi yêu cầu thu mua sách cũ thành công! Mã đơn: #BB-" + saved.getId() +
                            ". Nhân viên chi nhánh " + store.getName() + " sẽ liên hệ sớm nhất để thẩm định.");
            return "redirect:/?buybackSuccess=true#buyback";

        } catch (Exception e) {
            populateHomeModel(model, null, null, null, userDetails, buybackForm);
            model.addAttribute("buybackError", "Có lỗi xảy ra khi xử lý yêu cầu: " + e.getMessage());
            return "index";
        }
    }

    private void populateHomeModel(Model model, String keyword, String category, Long storeId,
                                   CustomUserDetails userDetails, CustomerBuybackDTO buybackDTO) {
        List<Book> allActiveBooks = bookRepository.findByIsActiveTrue();
        List<BookCopy> allAvailableCopies = bookCopyRepository.findByStatus(CopyStatus.AVAILABLE);

        // Gom nhóm copy theo bookId
        Map<Long, List<BookCopy>> copiesByBookId = allAvailableCopies.stream()
                .filter(c -> c.getBook() != null)
                .collect(Collectors.groupingBy(c -> c.getBook().getId()));

        List<BookCatalogItemDTO> catalog = new ArrayList<>();
        for (Book b : allActiveBooks) {
            List<BookCopy> copies = copiesByBookId.getOrDefault(b.getId(), Collections.emptyList());

            // Lọc theo storeId nếu có yêu cầu
            if (storeId != null) {
                boolean hasCopyInStore = copies.stream().anyMatch(c -> c.getStore() != null && c.getStore().getId().equals(storeId));
                if (!hasCopyInStore) {
                    continue;
                }
            }

            // Lọc theo keyword
            if (keyword != null && !keyword.isBlank()) {
                String kw = keyword.toLowerCase().trim();
                boolean matchesTitle = b.getTitle() != null && b.getTitle().toLowerCase().contains(kw);
                boolean matchesAuthor = b.getAuthor() != null && b.getAuthor().toLowerCase().contains(kw);
                boolean matchesCat = b.getCategory() != null && b.getCategory().toLowerCase().contains(kw);
                if (!matchesTitle && !matchesAuthor && !matchesCat) {
                    continue;
                }
            }

            // Lọc theo category
            if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
                if (b.getCategory() == null || !b.getCategory().equalsIgnoreCase(category.trim())) {
                    continue;
                }
            }

            BigDecimal minPrice = copies.stream()
                    .map(BookCopy::getPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(b.getCoverPrice());

            BigDecimal maxPrice = copies.stream()
                    .map(BookCopy::getPrice)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(b.getCoverPrice());

            List<String> storeNames = copies.stream()
                    .map(c -> c.getStore() != null ? c.getStore().getName() : "")
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            List<BookCopyResponseDTO> copyDTOs = copies.stream()
                    .map(c -> BookCopyResponseDTO.builder()
                            .id(c.getId())
                            .copyCode(c.getCopyCode())
                            .bookId(b.getId())
                            .bookTitle(b.getTitle())
                            .storeId(c.getStore() != null ? c.getStore().getId() : null)
                            .storeName(c.getStore() != null ? c.getStore().getName() : "")
                            .bookCondition(c.getBookCondition())
                            .status(c.getStatus())
                            .price(c.getPrice())
                            .note(c.getNote())
                            .build())
                    .collect(Collectors.toList());

            catalog.add(BookCatalogItemDTO.builder()
                    .id(b.getId())
                    .title(b.getTitle())
                    .author(b.getAuthor())
                    .publisher(b.getPublisher())
                    .publicationYear(b.getPublicationYear())
                    .category(b.getCategory())
                    .description(b.getDescription())
                    .coverPrice(b.getCoverPrice())
                    .coverImage(b.getCoverImage())
                    .availableCopiesCount(copies.size())
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .storeNames(storeNames)
                    .copies(copyDTOs)
                    .build());
        }

        List<Store> activeStores = storeRepository.findByIsActiveTrue();
        List<String> categories = bookRepository.findDistinctCategories();

        if (userDetails != null && userDetails.getUser() != null) {
            User u = userDetails.getUser();
            if (buybackDTO.getCustomerName() == null || buybackDTO.getCustomerName().isBlank()) {
                buybackDTO.setCustomerName(u.getFullName());
            }
            if (buybackDTO.getCustomerEmail() == null || buybackDTO.getCustomerEmail().isBlank()) {
                buybackDTO.setCustomerEmail(u.getEmail());
            }
            if (buybackDTO.getCustomerPhone() == null || buybackDTO.getCustomerPhone().isBlank()) {
                buybackDTO.setCustomerPhone(u.getPhone());
            }
        }

        model.addAttribute("catalog", catalog);
        model.addAttribute("stores", activeStores);
        model.addAttribute("categories", categories);
        model.addAttribute("conditions", com.bookstorechain.enums.BookCondition.values());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("buybackForm", buybackDTO);
    }
}