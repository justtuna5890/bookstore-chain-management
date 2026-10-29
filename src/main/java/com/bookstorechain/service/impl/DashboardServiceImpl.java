package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.OperationalAlertDTO;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.enums.TransferStatus;
import com.bookstorechain.repository.*;
import com.bookstorechain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
        stats.put("totalBookCopies", bookCopyRepository.countInStock());
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

    @Override
    @Transactional(readOnly = true)
    public List<BuybackRequest> getRecentBuybacks(Long storeId, int limit) {
        return buybackRequestRepository.searchRequests(storeId, null, PageRequest.of(0, limit, Sort.by("id").descending())).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransfer> getRecentTransfers(Long storeId, int limit) {
        return stockTransferRepository.searchTransfers(storeId, null, PageRequest.of(0, limit, Sort.by("id").descending())).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationalAlertDTO> getOperationalAlerts(Long storeId) {
        List<OperationalAlertDTO> alerts = new ArrayList<>();

        // 1. Cảnh báo yêu cầu thu mua PENDING quá 48h
        LocalDateTime threshold48h = LocalDateTime.now().minusHours(48);
        List<BuybackRequest> overdueBuybacks = buybackRequestRepository.findOverduePendingRequests(storeId, threshold48h);
        if (!overdueBuybacks.isEmpty()) {
            alerts.add(OperationalAlertDTO.builder()
                    .id("OVERDUE_BUYBACK")
                    .title("Yêu Cầu Thu Mua Chờ Duyệt Quá 48 Giờ")
                    .message("Có " + overdueBuybacks.size() + " yêu cầu thu mua đang chờ thẩm định/báo giá quá 48h.")
                    .level("DANGER")
                    .actionUrl("/admin/buybacks")
                    .actionLabel("Thẩm định ngay")
                    .category("BUYBACK")
                    .build());
        }

        // 2. Cảnh báo phiếu chuyển kho IN_TRANSIT quá 72h
        LocalDateTime threshold72h = LocalDateTime.now().minusHours(72);
        List<StockTransfer> delayedTransfers = stockTransferRepository.findDelayedInTransitTransfers(storeId, threshold72h);
        if (!delayedTransfers.isEmpty()) {
            alerts.add(OperationalAlertDTO.builder()
                    .id("DELAYED_TRANSFER")
                    .title("Phiếu Chuyển Kho Vận Chuyển Quá Hạn (72h)")
                    .message("Có " + delayedTransfers.size() + " phiếu chuyển kho đang vận chuyển hơn 72 giờ chưa nhập kho.")
                    .level("DANGER")
                    .actionUrl("/admin/transfers")
                    .actionLabel("Kiểm tra vận chuyển")
                    .category("TRANSFER")
                    .build());
        }

        // 3. Cảnh báo đầu sách sắp cạn tồn kho (<= 2 bản sách khả dụng)
        List<Object[]> lowStockBooks = bookCopyRepository.findLowStockBooks(storeId);
        if (!lowStockBooks.isEmpty()) {
            alerts.add(OperationalAlertDTO.builder()
                    .id("LOW_STOCK")
                    .title("Cảnh Báo Đầu Sách Sắp Hết Kho")
                    .message("Có " + lowStockBooks.size() + " tựa sách trong kho chỉ còn từ 1 đến 2 bản sách khả dụng.")
                    .level("WARNING")
                    .actionUrl("/admin/book-copies")
                    .actionLabel("Xem danh sách kho")
                    .category("INVENTORY")
                    .build());
        }

        // 4. Cảnh báo bản sách lưu kho quá 30 ngày chưa phát sinh giao dịch
        LocalDateTime threshold30d = LocalDateTime.now().minusDays(30);
        long staleCopiesCount = bookCopyRepository.countStaleCopies(storeId, threshold30d);
        if (staleCopiesCount > 0) {
            alerts.add(OperationalAlertDTO.builder()
                    .id("STALE_INVENTORY")
                    .title("Bản Sách Tồn Kho Lâu (> 30 Ngày)")
                    .message("Có " + staleCopiesCount + " bản sách khả dụng lưu kho trên 30 ngày chưa có phát sinh giao dịch hoặc điều chuyển.")
                    .level("INFO")
                    .actionUrl("/admin/book-copies")
                    .actionLabel("Rà soát tồn kho")
                    .category("INVENTORY")
                    .build());
        }

        return alerts;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getExtendedMetrics(Long storeId) {
        Map<String, Object> metrics = new HashMap<>();

        long pendingBuybacks = buybackRequestRepository.countByStatusAndStoreId(BuybackStatus.PENDING, storeId);
        metrics.put("pendingBuybacks", pendingBuybacks);

        long activeTransfers = stockTransferRepository.countByStatusesAndStoreId(
                List.of(TransferStatus.PENDING, TransferStatus.IN_TRANSIT), storeId);
        metrics.put("activeTransfers", activeTransfers);

        List<Object[]> lowStockBooks = bookCopyRepository.findLowStockBooks(storeId);
        metrics.put("lowStockCount", (long) lowStockBooks.size());

        long completedBuybacks = buybackRequestRepository.countByStatusAndStoreId(BuybackStatus.COMPLETED, storeId);
        long rejectedBuybacks = buybackRequestRepository.countByStatusAndStoreId(BuybackStatus.REJECTED, storeId);
        long resolvedBuybacks = completedBuybacks + rejectedBuybacks;
        double approvalRate = resolvedBuybacks > 0 ? ((double) completedBuybacks / resolvedBuybacks) * 100.0 : 0.0;
        metrics.put("buybackApprovalRate", Math.round(approvalRate * 10.0) / 10.0);
        metrics.put("completedBuybacks", completedBuybacks);
        metrics.put("rejectedBuybacks", rejectedBuybacks);

        LocalDateTime threshold30d = LocalDateTime.now().minusDays(30);
        long staleCopies = bookCopyRepository.countStaleCopies(storeId, threshold30d);
        metrics.put("staleCopiesCount", staleCopies);

        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTransferFlowMatrix() {
        Map<String, Long> matrix = new LinkedHashMap<>();
        List<Object[]> rows = stockTransferRepository.getCompletedTransferFlow();
        for (Object[] row : rows) {
            String sender = (String) row[0];
            String receiver = (String) row[1];
            Long count = ((Number) row[2]).longValue();
            String route = (sender != null ? sender : "Kho A") + " ➔ " + (receiver != null ? receiver : "Kho B");
            matrix.put(route, count);
        }
        return matrix;
    }
}
