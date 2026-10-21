package com.bookstorechain.dto.response;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    // Thu mua
    private long totalBuybacks;
    private long completedBuybacks;
    private long pendingBuybacks;
    private long rejectedBuybacks;
    private BigDecimal totalBuybackAmount;
    private List<BuybackRequest> buybackRequests;

    // Điều chuyển
    private long totalTransfers;
    private long completedTransfers;
    private long inTransitTransfers;
    private long pendingTransfers;
    private long totalCopiesTransferred;
    private List<StockTransfer> stockTransfers;

    // Tồn kho hiện tại
    private long totalBookCopies;
    private BigDecimal totalInventoryValue;
}
