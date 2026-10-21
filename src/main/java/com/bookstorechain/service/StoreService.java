package com.bookstorechain.service;

import com.bookstorechain.dto.request.StoreRequestDTO;
import com.bookstorechain.dto.response.StoreResponseDTO;

import java.util.List;

public interface StoreService {
    List<StoreResponseDTO> getAllStores();
    StoreResponseDTO getStoreById(Long id);
    void createStore(StoreRequestDTO requestDTO);
    void updateStore(Long id, StoreRequestDTO requestDTO);
    void toggleStoreStatus(Long id);
}