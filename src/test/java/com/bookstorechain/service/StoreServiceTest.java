package com.bookstorechain.service;

import com.bookstorechain.dto.request.StoreRequestDTO;
import com.bookstorechain.dto.response.StoreResponseDTO;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.TransferStatus;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.StockTransferRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.service.impl.StoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService Unit Tests")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StockTransferRepository stockTransferRepository;

    @InjectMocks
    private StoreServiceImpl storeService;

    private Store sampleStore;

    @BeforeEach
    void setUp() {
        sampleStore = Store.builder()
                .name("Chi nhánh Quận 1")
                .address("123 Lê Lợi, Quận 1, TP.HCM")
                .phone("0901234567")
                .isActive(true)
                .employees(new ArrayList<>())
                .build();
        sampleStore.setId(1L);
    }

    @Test
    @DisplayName("getAllStores - Trả về danh sách cửa hàng DTO")
    void getAllStores_ShouldReturnListOfStoreResponseDTO() {
        when(storeRepository.findAll()).thenReturn(List.of(sampleStore));

        List<StoreResponseDTO> result = storeService.getAllStores();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Chi nhánh Quận 1");
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(storeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getStoreById - Thành công khi tồn tại ID")
    void getStoreById_WhenExists_ShouldReturnStoreResponseDTO() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));

        StoreResponseDTO result = storeService.getStoreById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Chi nhánh Quận 1");
    }

    @Test
    @DisplayName("getStoreById - Ném ResourceNotFoundException khi không tìm thấy ID")
    void getStoreById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(storeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.getStoreById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy chi nhánh với ID: 99");
    }

    @Test
    @DisplayName("createStore - Ném DuplicateResourceException khi tên chi nhánh đã tồn tại")
    void createStore_WhenNameExists_ShouldThrowDuplicateResourceException() {
        StoreRequestDTO dto = new StoreRequestDTO();
        dto.setName("Chi nhánh Quận 1");
        dto.setAddress("456 CMT8");
        dto.setPhone("0909888999");

        when(storeRepository.existsByName("Chi nhánh Quận 1")).thenReturn(true);

        assertThatThrownBy(() -> storeService.createStore(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Tên chi nhánh đã tồn tại");

        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    @DisplayName("createStore - Tạo mới thành công khi tên chưa tồn tại")
    void createStore_WhenValid_ShouldSaveStore() {
        StoreRequestDTO dto = new StoreRequestDTO();
        dto.setName("Chi nhánh Mới");
        dto.setAddress("456 CMT8");
        dto.setPhone("0909888999");

        when(storeRepository.existsByName("Chi nhánh Mới")).thenReturn(false);

        storeService.createStore(dto);

        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("toggleStoreStatus - Ném IllegalStateException khi đang hoạt động nhưng còn phiếu chuyển kho chưa hoàn tất")
    void toggleStoreStatus_WhenActiveAndHasOngoingTransfers_ShouldThrowIllegalStateException() {
        sampleStore.setActive(true);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));
        when(stockTransferRepository.existsByStoreAndStatuses(
                eq(1L),
                eq(List.of(TransferStatus.PENDING, TransferStatus.IN_TRANSIT))
        )).thenReturn(true);

        assertThatThrownBy(() -> storeService.toggleStoreStatus(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không thể tạm dừng hoạt động chi nhánh đang có phiếu chuyển kho chưa hoàn tất");

        verify(storeRepository, never()).save(sampleStore);
    }

    @Test
    @DisplayName("toggleStoreStatus - Tắt hoạt động thành công khi không có phiếu chuyển kho đang xử lý")
    void toggleStoreStatus_WhenActiveAndNoOngoingTransfers_ShouldDeactivateStore() {
        sampleStore.setActive(true);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));
        when(stockTransferRepository.existsByStoreAndStatuses(
                eq(1L),
                eq(List.of(TransferStatus.PENDING, TransferStatus.IN_TRANSIT))
        )).thenReturn(false);

        storeService.toggleStoreStatus(1L);

        assertThat(sampleStore.isActive()).isFalse();
        verify(storeRepository, times(1)).save(sampleStore);
    }

    @Test
    @DisplayName("toggleStoreStatus - Kích hoạt lại chi nhánh đang tắt không cần check phiếu chuyển")
    void toggleStoreStatus_WhenInactive_ShouldActivateStoreDirectly() {
        sampleStore.setActive(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));

        storeService.toggleStoreStatus(1L);

        assertThat(sampleStore.isActive()).isTrue();
        verify(stockTransferRepository, never()).existsByStoreAndStatuses(any(), any());
        verify(storeRepository, times(1)).save(sampleStore);
    }
}
