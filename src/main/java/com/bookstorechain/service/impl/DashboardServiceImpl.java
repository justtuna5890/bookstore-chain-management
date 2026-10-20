package com.bookstorechain.service.impl;

import com.bookstorechain.repository.*;
import com.bookstorechain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StoreRepository storeRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BuybackRequestRepository buybackRequestRepository;
    private final StockTransferRepository stockTransferRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalStores", storeRepository.count());
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalBookCopies", bookCopyRepository.count());
        stats.put("totalBuybacks", buybackRequestRepository.count());
        stats.put("totalTransfers", stockTransferRepository.count());
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getInventoryByStore() {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Object[]> rows = bookCopyRepository.countCopiesByStore();
        for (Object[] row : rows) {
            String storeName = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            result.put(storeName != null ? storeName : "Chưa phân chi nhánh", count);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getInventoryValueByStore() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        List<Object[]> rows = bookCopyRepository.sumPriceByStore();
        for (Object[] row : rows) {
            String storeName = (String) row[0];
            BigDecimal sum = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            result.put(storeName != null ? storeName : "Chưa phân chi nhánh", sum);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getConditionBreakdown() {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Object[]> rows = bookCopyRepository.countByCondition();
        for (Object[] row : rows) {
            String condition = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = ((Number) row[1]).longValue();
            result.put(condition, count);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getBuybackStatusBreakdown() {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Object[]> rows = buybackRequestRepository.countByStatus();
        for (Object[] row : rows) {
            String status = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = ((Number) row[1]).longValue();
            result.put(status, count);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStatisticsByStore(Long storeId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalStores", 1L);
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalBookCopies", bookCopyRepository.countByStoreId(storeId));
        stats.put("totalBuybacks", buybackRequestRepository.countByStoreId(storeId));
        stats.put("totalTransfers", stockTransferRepository.countByStoreId(storeId));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getConditionBreakdownByStore(Long storeId) {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Object[]> rows = bookCopyRepository.countByConditionAndStore(storeId);
        for (Object[] row : rows) {
            String condition = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = ((Number) row[1]).longValue();
            result.put(condition, count);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getBuybackStatusBreakdownByStore(Long storeId) {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Object[]> rows = buybackRequestRepository.countByStatusAndStore(storeId);
        for (Object[] row : rows) {
            String status = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = ((Number) row[1]).longValue();
            result.put(status, count);
        }
        return result;
    }
}