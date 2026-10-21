package com.bookstorechain.service;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.entity.Store;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Long> getDashboardStatistics();
    
    /** Thống kê số lượng sách tồn kho theo từng chi nhánh */
    Map<String, Long> getInventoryByStore();

    /** Thống kê tổng giá trị tồn kho theo từng chi nhánh */
    Map<String, BigDecimal> getInventoryValueByStore();

    /** Thống kê tình trạng sách trong toàn hệ thống */
    Map<String, Long> getConditionBreakdown();

    /** Thống kê trạng thái yêu cầu thu mua */
    Map<String, Long> getBuybackStatusBreakdown();

    // ================= Dành cho phạm vi theo chi nhánh (STAFF/MANAGER chi nhánh) =================
    Map<String, Long> getDashboardStatisticsByStore(Long storeId);

    Map<String, Long> getConditionBreakdownByStore(Long storeId);

    Map<String, Long> getBuybackStatusBreakdownByStore(Long storeId);

    List<BuybackRequest> getRecentBuybacks(Long storeId, int limit);

    List<StockTransfer> getRecentTransfers(Long storeId, int limit);

    List<Store> getAllStores();
}
