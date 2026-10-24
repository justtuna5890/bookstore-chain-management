package com.bookstorechain.service;

import com.bookstorechain.dto.response.BuybackResponseDTO;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.enums.BuybackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BuybackService {
    Page<BuybackResponseDTO> getRequests(Long storeId, BuybackStatus status, Pageable pageable);
    BuybackRequest getRequestById(Long id);
    void updateStatusAndPricing(Long requestId, BuybackStatus status, String staffNote, java.util.List<Long> itemIds, java.util.List<java.math.BigDecimal> offeredPrices);
}