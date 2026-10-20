package com.bookstorechain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Long> getDashboardStatistics();
    
    /** Thống kê số lượng sách tồn kho theo từng chi nhánh */
    Map<String, Long> getInventoryByStore();

    /** Thống kê tổng giá trị tồn kho theo từng chi nhánh */
    Map<String, BigDecimal> getInventoryValueByStore();

    /** Thống kê tình trạng sách trong toàn hệ thống (NEW, LIKE_NEW, VERY_GOOD, GOOD, FAIR, POOR) */
    Map<String, Long> getConditionBreakdown();

    /** Thống kê trạng thái yêu cầu thu mua */
    Map<String, Long> getBuybackStatusBreakdown();

    // ================= Dành cho phạm vi theo chi nhánh (STAFF/MANAGER chi nhánh) =================
    Map<String, Long> getDashboardStatisticsByStore(Long storeId);

    Map<String, Long> getConditionBreakdownByStore(Long storeId);

    Map<String, Long> getBuybackStatusBreakdownByStore(Long storeId);
}