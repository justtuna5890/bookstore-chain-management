package com.bookstorechain.service;

import com.bookstorechain.dto.request.StockTransferRequestDTO;
import com.bookstorechain.entity.*;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.enums.TransferStatus;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.*;
import com.bookstorechain.service.impl.StockTransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockTransferService Unit Tests")
class StockTransferServiceTest {

    @Mock
    private StockTransferRepository stockTransferRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StockTransferServiceImpl stockTransferService;

    private Store senderStore;
    private Store receiverStore;
    private User adminUser;
    private User staffUser;
    private BookCopy availableCopy;

    @BeforeEach
    void setUp() {
        senderStore = Store.builder().name("Kho Hà Nội").build();
        senderStore.setId(1L);

        receiverStore = Store.builder().name("Kho Sài Gòn").build();
        receiverStore.setId(2L);

        adminUser = User.builder().username("admin").role(Role.ADMIN).build();
        adminUser.setId(10L);

        staffUser = User.builder().username("staff1").role(Role.STAFF).store(senderStore).build();
        staffUser.setId(11L);

        availableCopy = BookCopy.builder()
                .copyCode("BC-001")
                .store(senderStore)
                .status(CopyStatus.AVAILABLE)
                .build();
        availableCopy.setId(101L);
    }

    @Test
    @DisplayName("createTransfer - Bắt lỗi khi chi nhánh gửi và nhận trùng nhau")
    void createTransfer_WhenSameSenderAndReceiver_ShouldThrowIllegalArgumentException() {
        StockTransferRequestDTO dto = new StockTransferRequestDTO();
        dto.setSenderStoreId(1L);
        dto.setReceiverStoreId(1L);

        assertThatThrownBy(() -> stockTransferService.createTransfer(dto, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chi nhánh gửi và chi nhánh nhận không được trùng nhau!");

        verify(stockTransferRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTransfer - Bắt lỗi AccessDeniedException khi nhân viên cố tạo phiếu từ chi nhánh khác")
    void createTransfer_WhenStaffFromDifferentStore_ShouldThrowAccessDeniedException() {
        Store otherStore = Store.builder().name("Kho Đà Nẵng").build();
        otherStore.setId(3L);
        staffUser.setStore(otherStore); // Staff thuộc kho 3

        StockTransferRequestDTO dto = new StockTransferRequestDTO();
        dto.setSenderStoreId(1L); // Cố tình chuyển từ kho 1
        dto.setReceiverStoreId(2L);
        dto.setBookCopyIds(List.of(101L));

        when(storeRepository.findById(1L)).thenReturn(Optional.of(senderStore));
        when(storeRepository.findById(2L)).thenReturn(Optional.of(receiverStore));
        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staffUser));

        assertThatThrownBy(() -> stockTransferService.createTransfer(dto, "staff1"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Nhân viên chỉ có quyền tạo phiếu điều chuyển từ chi nhánh trực thuộc");
    }

    @Test
    @DisplayName("createTransfer - Bắt lỗi khi bản sách không thuộc kho gửi")
    void createTransfer_WhenBookCopyNotBelongToSenderStore_ShouldThrowIllegalArgumentException() {
        Store otherStore = Store.builder().name("Kho Khác").build();
        otherStore.setId(99L);
        availableCopy.setStore(otherStore);

        StockTransferRequestDTO dto = new StockTransferRequestDTO();
        dto.setSenderStoreId(1L);
        dto.setReceiverStoreId(2L);
        dto.setBookCopyIds(List.of(101L));

        when(storeRepository.findById(1L)).thenReturn(Optional.of(senderStore));
        when(storeRepository.findById(2L)).thenReturn(Optional.of(receiverStore));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookCopyRepository.findById(101L)).thenReturn(Optional.of(availableCopy));

        assertThatThrownBy(() -> stockTransferService.createTransfer(dto, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không thuộc kho chi nhánh gửi!");
    }

    @Test
    @DisplayName("createTransfer - Bắt lỗi khi bản sách không ở trạng thái AVAILABLE")
    void createTransfer_WhenBookCopyNotAvailable_ShouldThrowIllegalStateException() {
        availableCopy.setStatus(CopyStatus.SOLD);

        StockTransferRequestDTO dto = new StockTransferRequestDTO();
        dto.setSenderStoreId(1L);
        dto.setReceiverStoreId(2L);
        dto.setBookCopyIds(List.of(101L));

        when(storeRepository.findById(1L)).thenReturn(Optional.of(senderStore));
        when(storeRepository.findById(2L)).thenReturn(Optional.of(receiverStore));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookCopyRepository.findById(101L)).thenReturn(Optional.of(availableCopy));

        assertThatThrownBy(() -> stockTransferService.createTransfer(dto, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hiện không ở trạng thái sẵn sàng (AVAILABLE)");
    }

    @Test
    @DisplayName("createTransfer - Bắt lỗi khi bản sách đang nằm trong phiếu chuyển khác chưa hoàn tất")
    void createTransfer_WhenBookCopyInAnotherActiveTransfer_ShouldThrowIllegalStateException() {
        StockTransferRequestDTO dto = new StockTransferRequestDTO();
        dto.setSenderStoreId(1L);
        dto.setReceiverStoreId(2L);
        dto.setBookCopyIds(List.of(101L));

        when(storeRepository.findById(1L)).thenReturn(Optional.of(senderStore));
        when(storeRepository.findById(2L)).thenReturn(Optional.of(receiverStore));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookCopyRepository.findById(101L)).thenReturn(Optional.of(availableCopy));
        when(stockTransferRepository.isBookCopyInActiveTransfer(eq(101L), any())).thenReturn(true);

        assertThatThrownBy(() -> stockTransferService.createTransfer(dto, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("đang nằm trong một phiếu điều chuyển khác chưa hoàn tất!");
    }

    @Test
    @DisplayName("createTransfer - Tạo phiếu thành công: khóa sách HOLDING, lưu phiếu và bắn thông báo")
    void createTransfer_WhenValid_ShouldSetBookHoldingAndSaveTransferAndNotify() {
        StockTransferRequestDTO dto = new StockTransferRequestDTO();
        dto.setSenderStoreId(1L);
        dto.setReceiverStoreId(2L);
        dto.setBookCopyIds(List.of(101L));
        dto.setNote("Chuyển sách gấp");

        when(storeRepository.findById(1L)).thenReturn(Optional.of(senderStore));
        when(storeRepository.findById(2L)).thenReturn(Optional.of(receiverStore));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookCopyRepository.findById(101L)).thenReturn(Optional.of(availableCopy));
        when(stockTransferRepository.isBookCopyInActiveTransfer(eq(101L), any())).thenReturn(false);

        StockTransfer savedTransfer = StockTransfer.builder()
                .senderStore(senderStore)
                .receiverStore(receiverStore)
                .creator(adminUser)
                .status(TransferStatus.PENDING)
                .build();
        savedTransfer.setId(500L);
        when(stockTransferRepository.save(any(StockTransfer.class))).thenReturn(savedTransfer);

        stockTransferService.createTransfer(dto, "admin");

        assertThat(availableCopy.getStatus()).isEqualTo(CopyStatus.HOLDING);
        verify(bookCopyRepository, times(1)).save(availableCopy);
        verify(stockTransferRepository, times(1)).save(any(StockTransfer.class));
        verify(notificationService, times(1)).notifyNewStockTransfer(savedTransfer);
    }

    @Test
    @DisplayName("updateTransferStatus - Chặn sửa phiếu đã COMPLETED hoặc CANCELLED")
    void updateTransferStatus_WhenAlreadyFinal_ShouldThrowIllegalStateException() {
        StockTransfer completedTransfer = StockTransfer.builder()
                .status(TransferStatus.COMPLETED)
                .build();
        completedTransfer.setId(500L);

        when(stockTransferRepository.findById(500L)).thenReturn(Optional.of(completedTransfer));

        assertThatThrownBy(() -> stockTransferService.updateTransferStatus(500L, TransferStatus.CANCELLED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không thể thay đổi trạng thái của phiếu đã hoàn tất hoặc đã hủy!");
    }

    @Test
    @DisplayName("updateTransferStatus - Chặn nhảy cóc từ PENDING trực tiếp sang COMPLETED")
    void updateTransferStatus_WhenPendingToCompletedDirectly_ShouldThrowIllegalStateException() {
        StockTransfer pendingTransfer = StockTransfer.builder()
                .status(TransferStatus.PENDING)
                .build();
        pendingTransfer.setId(500L);

        when(stockTransferRepository.findById(500L)).thenReturn(Optional.of(pendingTransfer));

        assertThatThrownBy(() -> stockTransferService.updateTransferStatus(500L, TransferStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("phải qua trạng thái Đang vận chuyển (IN_TRANSIT) trước khi Hoàn tất (COMPLETED)");
    }

    @Test
    @DisplayName("updateTransferStatus - Chuyển sang IN_TRANSIT đổi trạng thái bản sách sang IN_TRANSIT")
    void updateTransferStatus_WhenTransitionToInTransit_ShouldUpdateBookCopiesToInTransit() {
        StockTransfer pendingTransfer = StockTransfer.builder()
                .status(TransferStatus.PENDING)
                .items(new ArrayList<>())
                .build();
        pendingTransfer.setId(500L);

        StockTransferItem item = StockTransferItem.builder()
                .stockTransfer(pendingTransfer)
                .bookCopy(availableCopy)
                .build();
        pendingTransfer.getItems().add(item);

        when(stockTransferRepository.findById(500L)).thenReturn(Optional.of(pendingTransfer));

        stockTransferService.updateTransferStatus(500L, TransferStatus.IN_TRANSIT);

        assertThat(pendingTransfer.getStatus()).isEqualTo(TransferStatus.IN_TRANSIT);
        assertThat(availableCopy.getStatus()).isEqualTo(CopyStatus.IN_TRANSIT);
        verify(bookCopyRepository, times(1)).save(availableCopy);
        verify(stockTransferRepository, times(1)).save(pendingTransfer);
    }

    @Test
    @DisplayName("updateTransferStatus - Chuyển sang COMPLETED gán sách cho chi nhánh nhận và trả về AVAILABLE")
    void updateTransferStatus_WhenTransitionToCompleted_ShouldUpdateBookCopiesStoreAndSetAvailable() {
        StockTransfer inTransitTransfer = StockTransfer.builder()
                .senderStore(senderStore)
                .receiverStore(receiverStore)
                .status(TransferStatus.IN_TRANSIT)
                .items(new ArrayList<>())
                .build();
        inTransitTransfer.setId(500L);

        availableCopy.setStatus(CopyStatus.IN_TRANSIT);
        StockTransferItem item = StockTransferItem.builder()
                .stockTransfer(inTransitTransfer)
                .bookCopy(availableCopy)
                .build();
        inTransitTransfer.getItems().add(item);

        when(stockTransferRepository.findById(500L)).thenReturn(Optional.of(inTransitTransfer));

        stockTransferService.updateTransferStatus(500L, TransferStatus.COMPLETED);

        assertThat(inTransitTransfer.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(availableCopy.getStore()).isEqualTo(receiverStore);
        assertThat(availableCopy.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
        verify(bookCopyRepository, times(1)).save(availableCopy);
        verify(stockTransferRepository, times(1)).save(inTransitTransfer);
    }

    @Test
    @DisplayName("updateTransferStatus - Hủy phiếu (CANCELLED) trả lại trạng thái AVAILABLE tại kho gửi")
    void updateTransferStatus_WhenTransitionToCancelled_ShouldRevertBookCopiesToAvailable() {
        StockTransfer inTransitTransfer = StockTransfer.builder()
                .senderStore(senderStore)
                .receiverStore(receiverStore)
                .status(TransferStatus.IN_TRANSIT)
                .items(new ArrayList<>())
                .build();
        inTransitTransfer.setId(500L);

        availableCopy.setStatus(CopyStatus.IN_TRANSIT);
        StockTransferItem item = StockTransferItem.builder()
                .stockTransfer(inTransitTransfer)
                .bookCopy(availableCopy)
                .build();
        inTransitTransfer.getItems().add(item);

        when(stockTransferRepository.findById(500L)).thenReturn(Optional.of(inTransitTransfer));

        stockTransferService.updateTransferStatus(500L, TransferStatus.CANCELLED);

        assertThat(inTransitTransfer.getStatus()).isEqualTo(TransferStatus.CANCELLED);
        assertThat(availableCopy.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
        assertThat(availableCopy.getStore()).isEqualTo(senderStore); // Vẫn thuộc kho gửi
        verify(bookCopyRepository, times(1)).save(availableCopy);
        verify(stockTransferRepository, times(1)).save(inTransitTransfer);
    }
}
