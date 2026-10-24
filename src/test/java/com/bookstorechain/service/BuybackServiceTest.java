package com.bookstorechain.service;

import com.bookstorechain.entity.*;
import com.bookstorechain.enums.BookCondition;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.impl.BuybackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuybackService Unit Tests")
class BuybackServiceTest {

    @Mock
    private BuybackRequestRepository buybackRequestRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BuybackServiceImpl buybackService;

    private BuybackRequest sampleRequest;
    private User sampleCustomer;
    private Store sampleStore;

    @BeforeEach
    void setUp() {
        sampleCustomer = User.builder()
                .username("nguyenvana")
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .build();
        sampleCustomer.setId(10L);

        sampleStore = Store.builder()
                .name("Chi nhánh Quận 1")
                .build();
        sampleStore.setId(1L);

        sampleRequest = BuybackRequest.builder()
                .customer(sampleCustomer)
                .store(sampleStore)
                .status(BuybackStatus.PENDING)
                .items(new ArrayList<>())
                .build();
        sampleRequest.setId(100L);
    }

    @Test
    @DisplayName("getRequestById - Thành công khi tồn tại ID")
    void getRequestById_WhenExists_ShouldReturnRequest() {
        when(buybackRequestRepository.findById(100L)).thenReturn(Optional.of(sampleRequest));

        BuybackRequest result = buybackService.getRequestById(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(BuybackStatus.PENDING);
    }

    @Test
    @DisplayName("getRequestById - Ném ResourceNotFoundException khi không tồn tại ID")
    void getRequestById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(buybackRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buybackService.getRequestById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Khong tim thay yeu cau thu mua voi ID: 999");
    }

    @Test
    @DisplayName("updateStatusAndPricing - Ném IllegalStateException khi đơn đã COMPLETED")
    void updateStatusAndPricing_WhenAlreadyCompleted_ShouldThrowIllegalStateException() {
        sampleRequest.setStatus(BuybackStatus.COMPLETED);
        when(buybackRequestRepository.findById(100L)).thenReturn(Optional.of(sampleRequest));

        assertThatThrownBy(() -> buybackService.updateStatusAndPricing(100L, BuybackStatus.APPROVED, "Ghi chú", null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Đơn thu mua đã COMPLETED, không thể thay đổi trạng thái!");

        verify(buybackRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatusAndPricing - Ném IllegalStateException khi đơn đã REJECTED")
    void updateStatusAndPricing_WhenAlreadyRejected_ShouldThrowIllegalStateException() {
        sampleRequest.setStatus(BuybackStatus.REJECTED);
        when(buybackRequestRepository.findById(100L)).thenReturn(Optional.of(sampleRequest));

        assertThatThrownBy(() -> buybackService.updateStatusAndPricing(100L, BuybackStatus.APPROVED, "Ghi chú", null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Đơn thu mua đã REJECTED, không thể thay đổi trạng thái!");

        verify(buybackRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatusAndPricing - Ném IllegalStateException khi chuyển sang COMPLETED mà chưa qua APPROVED")
    void updateStatusAndPricing_WhenDirectlyToCompletedWithoutApproval_ShouldThrowIllegalStateException() {
        sampleRequest.setStatus(BuybackStatus.PENDING);
        when(buybackRequestRepository.findById(100L)).thenReturn(Optional.of(sampleRequest));

        assertThatThrownBy(() -> buybackService.updateStatusAndPricing(100L, BuybackStatus.COMPLETED, "Hoàn tất trực tiếp", null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("phải qua bước phê duyệt (APPROVED) và định giá trước khi Hoàn tất (COMPLETED)");

        verify(buybackRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatusAndPricing - Ném IllegalStateException khi cố tình đảo ngược về PENDING")
    void updateStatusAndPricing_WhenRevertingToPending_ShouldThrowIllegalStateException() {
        sampleRequest.setStatus(BuybackStatus.APPROVED);
        when(buybackRequestRepository.findById(100L)).thenReturn(Optional.of(sampleRequest));

        assertThatThrownBy(() -> buybackService.updateStatusAndPricing(100L, BuybackStatus.PENDING, "Quay lại pending", null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không thể quay lui trạng thái về Chờ duyệt (PENDING)");

        verify(buybackRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatusAndPricing - Thành công chuyển APPROVED sang COMPLETED và tạo BookCopy nhập kho")
    void updateStatusAndPricing_WhenApprovedToCompleted_ShouldCreateBookCopiesAndSave() {
        sampleRequest.setStatus(BuybackStatus.APPROVED);

        BuybackItem item = BuybackItem.builder()
                .bookTitle("Lập trình Java")
                .bookAuthor("Nguyễn Văn B")
                .proposedCondition(BookCondition.GOOD)
                .offeredPrice(new BigDecimal("50000"))
                .build();
        item.setId(201L);
        sampleRequest.getItems().add(item);

        when(buybackRequestRepository.findById(100L)).thenReturn(Optional.of(sampleRequest));
        when(bookCopyRepository.existsByCopyCode("BB-100-201")).thenReturn(false);
        when(bookRepository.existsByTitleAndAuthor("Lập trình Java", "Nguyễn Văn B")).thenReturn(false);

        Book savedBook = Book.builder()
                .title("Lập trình Java")
                .author("Nguyễn Văn B")
                .build();
        savedBook.setId(50L);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        buybackService.updateStatusAndPricing(100L, BuybackStatus.COMPLETED, "Hoàn tất thu mua", null, null);

        assertThat(sampleRequest.getStatus()).isEqualTo(BuybackStatus.COMPLETED);
        verify(bookCopyRepository, times(1)).save(any(BookCopy.class));
        verify(buybackRequestRepository, times(1)).save(sampleRequest);
    }
}
