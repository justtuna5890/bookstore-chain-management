package com.bookstorechain.service;

import com.bookstorechain.dto.request.CustomerBuybackDTO;
import com.bookstorechain.dto.response.BuybackResponseDTO;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.BuybackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BuybackService {
    Page<BuybackResponseDTO> getRequests(Long storeId, BuybackStatus status, Pageable pageable);
    BuybackRequest getRequestById(Long id);
    void updateStatusAndPricing(Long requestId, BuybackStatus status, String staffNote, List<Long> itemIds, List<BigDecimal> offeredPrices);
    BuybackRequest createCustomerBuyback(CustomerBuybackDTO dto, User loggedInUser);
}
