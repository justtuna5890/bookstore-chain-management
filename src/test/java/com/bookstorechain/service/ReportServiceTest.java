package com.bookstorechain.service;

import com.bookstorechain.dto.response.ReportSummaryDTO;
import com.bookstorechain.entity.*;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.enums.TransferStatus;
import com.bookstorechain.repository.*;
import com.bookstorechain.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Unit Tests")
class ReportServiceTest {

    @Mock
    private BuybackRequestRepository buybackRequestRepository;

    @Mock
    private StockTransferRepository stockTransferRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private SalesStatsService salesStatsService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    @DisplayName("generateReport - Tổng hợp đúng số liệu báo cáo toàn hệ thống theo kỳ")
    void generateReport_ShouldAggregateCorrectly() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        // Giả lập 1 đơn thu mua COMPLETED với 1 item giá 50000
        BuybackItem item = BuybackItem.builder()
                .bookTitle("Lập trình Web")
                .offeredPrice(new BigDecimal("50000"))
                .build();
        User customer = User.builder().fullName("Khách Hàng A").email("khacha@test.com").build();
        customer.setId(10L);

        BuybackRequest br = BuybackRequest.builder()
                .customer(customer)
                .status(BuybackStatus.COMPLETED)
                .items(List.of(item))
                .build();

        // Giả lập 1 phiếu chuyển kho COMPLETED với 2 bản sách
        Store s1 = Store.builder().name("Kho Hà Nội").build();
        Store s2 = Store.builder().name("Kho Sài Gòn").build();
        StockTransferItem transferItem1 = StockTransferItem.builder().build();
        StockTransferItem transferItem2 = StockTransferItem.builder().build();
        StockTransfer st = StockTransfer.builder()
                .senderStore(s1)
                .receiverStore(s2)
                .status(TransferStatus.COMPLETED)
                .items(List.of(transferItem1, transferItem2))
                .build();

        when(buybackRequestRepository.findForReport(eq(null), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(br));
        when(stockTransferRepository.findForReport(eq(null), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(st));
        when(dashboardService.getInventoryValueByStore())
                .thenReturn(Map.of("Kho Hà Nội", new BigDecimal("100000000")));
        when(bookCopyRepository.countInStock()).thenReturn(500L);
        when(bookCopyRepository.countByConditionAndOptionalStore(null)).thenReturn(Collections.emptyList());
        when(bookCopyRepository.countByStatusAndStoreIdGrouped(null)).thenReturn(Collections.emptyList());
        when(bookCopyRepository.findLowStockBooks(null)).thenReturn(Collections.emptyList());
        when(bookCopyRepository.countStaleCopies(eq(null), any())).thenReturn(5L);
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(120L);
        when(userRepository.countByRoleAndCreatedAtBetween(eq(Role.CUSTOMER), any(), any())).thenReturn(15L);

        ReportSummaryDTO report = reportService.generateReport(startDate, endDate, null);

        assertThat(report).isNotNull();
        assertThat(report.getTotalBuybacks()).isEqualTo(1L);
        assertThat(report.getCompletedBuybacks()).isEqualTo(1L);
        assertThat(report.getTotalBuybackAmount()).isEqualByComparingTo("50000");
        assertThat(report.getTotalTransfers()).isEqualTo(1L);
        assertThat(report.getCompletedTransfers()).isEqualTo(1L);
        assertThat(report.getTotalCopiesTransferred()).isEqualTo(2L);
        assertThat(report.getTotalBookCopies()).isEqualTo(500L);
        assertThat(report.getTotalCustomers()).isEqualTo(120L);
        assertThat(report.getNewCustomersInPeriod()).isEqualTo(15L);
        assertThat(report.getStaleCopiesCount()).isEqualTo(5L);
    }
}
