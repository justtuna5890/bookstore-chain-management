package com.bookstorechain.service;

import com.bookstorechain.dto.response.OperationalAlertDTO;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.repository.*;
import com.bookstorechain.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private BuybackRequestRepository buybackRequestRepository;

    @Mock
    private StockTransferRepository stockTransferRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("getDashboardStatistics - Trả về đúng số lượng tổng hợp hệ thống")
    void getDashboardStatistics_ShouldReturnCorrectCounts() {
        when(storeRepository.count()).thenReturn(3L);
        when(bookRepository.count()).thenReturn(50L);
        when(bookCopyRepository.count()).thenReturn(200L);
        when(buybackRequestRepository.count()).thenReturn(15L);
        when(stockTransferRepository.count()).thenReturn(8L);

        Map<String, Long> stats = dashboardService.getDashboardStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats.get("totalStores")).isEqualTo(3L);
        assertThat(stats.get("totalBooks")).isEqualTo(50L);
        assertThat(stats.get("totalBookCopies")).isEqualTo(200L);
        assertThat(stats.get("totalBuybacks")).isEqualTo(15L);
        assertThat(stats.get("totalTransfers")).isEqualTo(8L);
    }

    @Test
    @DisplayName("getOperationalAlerts - Phát hiện cảnh báo DANGER khi có đơn thu mua quá 48h")
    void getOperationalAlerts_WhenHasOverdueBuybacks_ShouldIncludeDangerAlert() {
        BuybackRequest overdueRequest = new BuybackRequest();
        when(buybackRequestRepository.findOverduePendingRequests(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueRequest));
        when(stockTransferRepository.findDelayedInTransitTransfers(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookCopyRepository.findLowStockBooks(eq(1L)))
                .thenReturn(Collections.emptyList());
        when(bookCopyRepository.countStaleCopies(eq(1L), any(LocalDateTime.class)))
                .thenReturn(0L);

        List<OperationalAlertDTO> alerts = dashboardService.getOperationalAlerts(1L);

        assertThat(alerts).hasSize(1);
        OperationalAlertDTO alert = alerts.get(0);
        assertThat(alert.getId()).isEqualTo("OVERDUE_BUYBACK");
        assertThat(alert.getLevel()).isEqualTo("DANGER");
        assertThat(alert.getCategory()).isEqualTo("BUYBACK");
    }

    @Test
    @DisplayName("getOperationalAlerts - Phát hiện cảnh báo DANGER khi phiếu chuyển kho IN_TRANSIT quá 72h")
    void getOperationalAlerts_WhenHasDelayedTransfers_ShouldIncludeDangerAlert() {
        when(buybackRequestRepository.findOverduePendingRequests(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(stockTransferRepository.findDelayedInTransitTransfers(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(new StockTransfer()));
        when(bookCopyRepository.findLowStockBooks(eq(1L)))
                .thenReturn(Collections.emptyList());
        when(bookCopyRepository.countStaleCopies(eq(1L), any(LocalDateTime.class)))
                .thenReturn(0L);

        List<OperationalAlertDTO> alerts = dashboardService.getOperationalAlerts(1L);

        assertThat(alerts).hasSize(1);
        OperationalAlertDTO alert = alerts.get(0);
        assertThat(alert.getId()).isEqualTo("DELAYED_TRANSFER");
        assertThat(alert.getLevel()).isEqualTo("DANGER");
        assertThat(alert.getCategory()).isEqualTo("TRANSFER");
    }

    @Test
    @DisplayName("getOperationalAlerts - Phát hiện cảnh báo WARNING khi có đầu sách sắp hết tồn kho")
    void getOperationalAlerts_WhenHasLowStockBooks_ShouldIncludeWarningAlert() {
        when(buybackRequestRepository.findOverduePendingRequests(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(stockTransferRepository.findDelayedInTransitTransfers(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        List<Object[]> lowStock = Collections.singletonList(new Object[]{"Sách A", 1L});
        when(bookCopyRepository.findLowStockBooks(eq(1L)))
                .thenReturn(lowStock);
        when(bookCopyRepository.countStaleCopies(eq(1L), any(LocalDateTime.class)))
                .thenReturn(0L);

        List<OperationalAlertDTO> alerts = dashboardService.getOperationalAlerts(1L);

        assertThat(alerts).hasSize(1);
        OperationalAlertDTO alert = alerts.get(0);
        assertThat(alert.getId()).isEqualTo("LOW_STOCK");
        assertThat(alert.getLevel()).isEqualTo("WARNING");
        assertThat(alert.getCategory()).isEqualTo("INVENTORY");
    }

    @Test
    @DisplayName("getOperationalAlerts - Trả về rỗng khi không có vấn đề vận hành nào")
    void getOperationalAlerts_WhenNoIssues_ShouldReturnEmptyList() {
        when(buybackRequestRepository.findOverduePendingRequests(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(stockTransferRepository.findDelayedInTransitTransfers(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookCopyRepository.findLowStockBooks(eq(1L)))
                .thenReturn(Collections.emptyList());
        when(bookCopyRepository.countStaleCopies(eq(1L), any(LocalDateTime.class)))
                .thenReturn(0L);

        List<OperationalAlertDTO> alerts = dashboardService.getOperationalAlerts(1L);

        assertThat(alerts).isEmpty();
    }
}
