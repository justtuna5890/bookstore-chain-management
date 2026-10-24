package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ReportSummaryDTO;
import com.bookstorechain.dto.response.SalesReportDTO;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StockTransferRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.DashboardService;
import com.bookstorechain.service.ReportService;
import com.bookstorechain.service.SalesStatsService;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Doanh thu bán hàng online đi vào báo cáo của nhóm A: dữ liệu báo cáo, file Excel và file PDF. */
@ExtendWith(MockitoExtension.class)
class ReportSalesIntegrationTest {

    private static final LocalDate START = LocalDate.of(2026, 10, 1);
    private static final LocalDate END = LocalDate.of(2026, 10, 3);

    @Mock private BuybackRequestRepository buybackRequestRepository;
    @Mock private StockTransferRepository stockTransferRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private UserRepository userRepository;
    @Mock private DashboardService dashboardService;
    @Mock private SalesStatsService salesStatsService;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(buybackRequestRepository, stockTransferRepository, storeRepository,
                bookCopyRepository, userRepository, dashboardService, salesStatsService);
    }

    @Test
    @DisplayName("Báo cáo có phần doanh thu cùng kỳ và đúng phạm vi chi nhánh")
    void reportCarriesSalesForTheSamePeriodAndScope() {
        SalesReportDTO sales = SalesReportExcelTest.sample();
        when(salesStatsService.getReport(START, END, 3L)).thenReturn(sales);

        ReportSummaryDTO summary = reportService.generateReport(START, END, 3L);

        assertThat(summary.getSales()).isSameAs(sales);
    }

    @Test
    @DisplayName("Khoảng thời gian dài hơn 366 ngày: không tính doanh thu (null), phần còn lại của báo cáo vẫn có")
    void tooLongRangeSkipsSales() {
        ReportSummaryDTO summary = reportService.generateReport(START.minusYears(2), END, null);

        assertThat(summary.getSales()).isNull();
        verify(salesStatsService, never()).getReport(any(), any(), any());
        assertThat(summary.getBuybackRequests()).isNotNull();
    }

    @Test
    @DisplayName("Excel: thêm sheet 'Doanh Thu Ban Hang' khi có doanh thu và không thêm khi không có")
    void excelHasSalesSheetOnlyWhenAvailable() throws Exception {
        when(salesStatsService.getReport(START, END, null)).thenReturn(SalesReportExcelTest.sample());
        MockHttpServletResponse with = new MockHttpServletResponse();
        reportService.exportReportExcel(START, END, null, with);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(with.getContentAsByteArray()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
            assertThat(workbook.getSheetName(3)).isEqualTo("Doanh Thu Ban Hang");
            assertThat(workbook.getSheetAt(0).getLastRowNum()).as("sheet tổng quan có thêm mục doanh thu").isGreaterThan(20);
        }

        MockHttpServletResponse without = new MockHttpServletResponse();
        reportService.exportReportExcel(START.minusYears(2), END, null, without);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(without.getContentAsByteArray()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);
        }
    }

    // ------------------------------------------------------------------ PDF

    @Test
    @DisplayName("Bỏ dấu tiếng Việt cho PDF: mọi nguyên âm có dấu, chữ đ/Đ; null thành chuỗi rỗng")
    void asciiStripsVietnameseMarks() {
        assertThat(PdfExportServiceImpl.ascii("Trăm Năm Cô Đơn")).isEqualTo("Tram Nam Co Don");
        assertThat(PdfExportServiceImpl.ascii("Hoàng Tử Bé — Đà Nẵng, Sài Gòn")).isEqualTo("Hoang Tu Be — Da Nang, Sai Gon");
        assertThat(PdfExportServiceImpl.ascii("Nguyễn Thị Minh Khai")).isEqualTo("Nguyen Thi Minh Khai");
        assertThat(PdfExportServiceImpl.ascii(null)).isEmpty();
    }

    private ReportSummaryDTO summaryWith(SalesReportDTO sales) {
        return ReportSummaryDTO.builder().startDate(START).endDate(END).storeName("Toan He Thong")
                .totalBuybackAmount(BigDecimal.ZERO).totalInventoryValue(BigDecimal.ZERO)
                .buybackRequests(List.of()).stockTransfers(List.of()).sales(sales).build();
    }

    private String pdfText(ReportSummaryDTO summary) throws Exception {
        ReportService stub = org.mockito.Mockito.mock(ReportService.class);
        when(stub.generateReport(START, END, null)).thenReturn(summary);
        MockHttpServletResponse response = new MockHttpServletResponse();
        new PdfExportServiceImpl(stockTransferRepository, buybackRequestRepository, stub).exportPeriodicReportPdf(START, END, null, response);

        byte[] bytes = response.getContentAsByteArray();
        assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        PdfReader reader = new PdfReader(bytes);
        StringBuilder text = new StringBuilder();
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            text.append(extractor.getTextFromPage(page)).append('\n');
        }
        return text.toString();
    }

    @Test
    @DisplayName("PDF: mục IV doanh thu có chỉ số, bảng chi nhánh và sách bán chạy đã bỏ dấu tiếng Việt")
    void pdfHasSalesSection() throws Exception {
        String text = pdfText(summaryWith(SalesReportExcelTest.sample()));

        assertThat(text).contains("IV. DOANH THU BAN HANG ONLINE");
        assertThat(text).contains("Doanh thu:").contains("So don hoan tat:").contains("3");
        assertThat(text).contains("OldBook Ha Noi").contains("OldBook Sai Gon");
        assertThat(text).contains("Sach Ban Chay").contains("Tram Nam Co Don").contains("Nha Gia Kim");
    }

    @Test
    @DisplayName("PDF: kỳ không có đơn hoàn tất hiện thông báo; khoảng quá dài hiện lý do thay vì số liệu")
    void pdfExplainsMissingSales() throws Exception {
        SalesReportDTO empty = SalesReportDTO.builder().startDate(START).endDate(END).build();
        assertThat(pdfText(summaryWith(empty))).contains("Khong co don hang nao hoan tat trong ky bao cao");
        assertThat(pdfText(summaryWith(null))).contains("Khong hien thi doanh thu vi khoang thoi gian dai hon 366 ngay");
    }
}
