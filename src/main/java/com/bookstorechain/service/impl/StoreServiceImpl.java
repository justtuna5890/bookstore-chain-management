package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.StoreRequestDTO;
import com.bookstorechain.dto.response.StoreResponseDTO;
import com.bookstorechain.entity.Store;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final com.bookstorechain.repository.StockTransferRepository stockTransferRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StoreResponseDTO> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponseDTO getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));
        return mapToResponseDTO(store);
    }

    @Override
    @Transactional
    public void createStore(StoreRequestDTO requestDTO) {
        if (storeRepository.existsByName(requestDTO.getName().trim())) {
            throw new DuplicateResourceException("Tên chi nhánh đã tồn tại trong hệ thống");
        }

        Store store = Store.builder()
                .name(requestDTO.getName().trim())
                .address(requestDTO.getAddress().trim())
                .phone(requestDTO.getPhone().trim())
                .isActive(true)
                .build();

        storeRepository.save(store);
    }

    @Override
    @Transactional
    public void updateStore(Long id, StoreRequestDTO requestDTO) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));

        String newName = requestDTO.getName().trim();
        if (!store.getName().equalsIgnoreCase(newName) && storeRepository.existsByName(newName)) {
            throw new DuplicateResourceException("Tên chi nhánh đã tồn tại trong hệ thống");
        }

        store.setName(newName);
        store.setAddress(requestDTO.getAddress().trim());
        store.setPhone(requestDTO.getPhone().trim());

        storeRepository.save(store);
    }

    @Override
    @Transactional
    public void toggleStoreStatus(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));

        if (store.isActive()) {
            boolean hasOngoingTransfers = stockTransferRepository.existsByStoreAndStatuses(
                    id,
                    java.util.List.of(
                            com.bookstorechain.enums.TransferStatus.PENDING,
                            com.bookstorechain.enums.TransferStatus.IN_TRANSIT
                    )
            );
            if (hasOngoingTransfers) {
                throw new IllegalStateException("Không thể tạm dừng hoạt động chi nhánh đang có phiếu chuyển kho chưa hoàn tất!");
            }
        }

        store.setActive(!store.isActive());
        storeRepository.save(store);
    }

    private StoreResponseDTO mapToResponseDTO(Store store) {
        return StoreResponseDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .isActive(store.isActive())
                .employeeCount(store.getEmployees() != null ? store.getEmployees().size() : 0)
                .createdAt(store.getCreatedAt())
                .build();
    }
}