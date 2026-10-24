package com.bookstorechain.service.impl;

import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StockTransferRepository;
import com.bookstorechain.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Chỉ kiểm tra việc tồn kho không tính sách đã bán cho khách; các thống kê còn lại thuộc phần của nhóm A. */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private StoreRepository storeRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private BuybackRequestRepository buybackRequestRepository;
    @Mock private StockTransferRepository stockTransferRepository;

    @Test
    @DisplayName("Số bản sao toàn chuỗi trên dashboard là số còn trong kho, không phải mọi bản sao kể cả đã bán")
    void chainStatisticsCountOnlyCopiesStillInStock() {
        DashboardServiceImpl service = new DashboardServiceImpl(storeRepository, bookRepository, bookCopyRepository,
                buybackRequestRepository, stockTransferRepository);
        when(bookCopyRepository.countInStock()).thenReturn(42L);

        Map<String, Long> stats = service.getDashboardStatistics();

        assertThat(stats.get("totalBookCopies")).isEqualTo(42L);
        verify(bookCopyRepository, never()).count();
    }

    @Test
    @DisplayName("Số bản sao của một chi nhánh cũng lấy theo truy vấn tồn kho")
    void storeStatisticsUseTheInStockCount() {
        DashboardServiceImpl service = new DashboardServiceImpl(storeRepository, bookRepository, bookCopyRepository,
                buybackRequestRepository, stockTransferRepository);
        when(bookCopyRepository.countByStoreId(3L)).thenReturn(11L);

        assertThat(service.getDashboardStatisticsByStore(3L).get("totalBookCopies")).isEqualTo(11L);
    }
}
