package com.bookstorechain.service;

import com.bookstorechain.dto.request.StockTransferRequestDTO;
import com.bookstorechain.dto.response.StockTransferResponseDTO;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockTransferService {
    Page<StockTransferResponseDTO> getTransfers(Long storeId, TransferStatus status, Pageable pageable);
    StockTransfer getTransferById(Long id);
    void createTransfer(StockTransferRequestDTO requestDTO, String currentUsername);
    void updateTransferStatus(Long id, TransferStatus newStatus);
}