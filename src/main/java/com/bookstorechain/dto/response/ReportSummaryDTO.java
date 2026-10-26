package com.bookstorechain.dto.response;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long storeId;
    private String storeName;

    // Phân hệ Thu mua sách cũ
    private long totalBuybacks;
    private long completedBuybacks;
    private long pendingBuybacks;
    private long rejectedBuybacks;
    private double buybackApprovalRate;
    private BigDecimal totalBuybackAmount;
    private List<BuybackRequest> buybackRequests;

    // Phân hệ Điều chuyển kho
    private long totalTransfers;
    private long completedTransfers;
    private long inTransitTransfers;
    private long pendingTransfers;
    private long cancelledTransfers;
    private long totalCopiesTransferred;
    private List<StockTransfer> stockTransfers;
    private Map<String, Long> transferFlowMatrix;

    // Phân hệ Tồn kho hiện tại
    private long totalBookCopies;
    private BigDecimal totalInventoryValue;
    private Map<String, Long> copiesByCondition;
    private Map<String, Long> copiesByStatus;
    private List<Object[]> lowStockBooks;
    private long staleCopiesCount;

    // Phân hệ Khách hàng & Bạn đọc
    private long totalCustomers;
    private long newCustomersInPeriod;
    private List<CustomerActivityDTO> customerActivities;

    // Phân hệ Doanh thu bán hàng online; null khi khoảng thời gian dài hơn mức hỗ trợ
    private SalesReportDTO sales;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerActivityDTO {
        private Long customerId;
        private String fullName;
        private String email;
        private String phone;
        private long totalRequests;
        private long completedRequests;
        private BigDecimal totalPayout;
    }
}
