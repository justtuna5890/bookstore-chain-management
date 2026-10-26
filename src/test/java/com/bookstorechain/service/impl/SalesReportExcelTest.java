package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.SalesReportDTO;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SalesReportExcelTest {

    static SalesReportDTO sample() {
        Map<String, BigDecimal> byStore = new LinkedHashMap<>();
        byStore.put("OldBook Ha Noi", new BigDecimal("500000"));
        byStore.put("OldBook Sai Gon", new BigDecimal("200000"));
        Map<String, BigDecimal> byPayment = new LinkedHashMap<>();
        byPayment.put("Chuyển khoản", new BigDecimal("450000"));
        byPayment.put("Tiền mặt khi nhận hàng", new BigDecimal("250000"));
        return SalesReportDTO.builder()
                .startDate(LocalDate.of(2026, 10, 1)).endDate(LocalDate.of(2026, 10, 3))
                .revenue(new BigDecimal("700000")).orderCount(3).booksSold(4)
                .discountTotal(new BigDecimal("20000")).shippingTotal(new BigDecimal("50000"))
                .daily(List.of(
                        new SalesReportDTO.DailyRevenue(LocalDate.of(2026, 10, 1), new BigDecimal("400000"), 2),
                        new SalesReportDTO.DailyRevenue(LocalDate.of(2026, 10, 2), BigDecimal.ZERO, 0),
                        new SalesReportDTO.DailyRevenue(LocalDate.of(2026, 10, 3), new BigDecimal("300000"), 1)))
                .revenueByStore(byStore).revenueByPayment(byPayment)
                .topBooks(List.of(new SalesReportDTO.TopBook(7L, "Trăm Năm Cô Đơn", 3, new BigDecimal("450000")),
                        new SalesReportDTO.TopBook(2L, "Nhà Giả Kim", 1, new BigDecimal("250000"))))
                .build();
    }

    /** Dòng đầu tiên có ô cột A bằng nhãn cho trước. */
    private Row rowLabelled(Sheet sheet, String label) {
        for (Row row : sheet) {
            if (row.getCell(0) != null && label.equals(row.getCell(0).toString())) {
                return row;
            }
        }
        throw new AssertionError("Không có dòng '" + label + "'");
    }

    @Test
    @DisplayName("Sheet doanh thu: số liệu tổng hợp là ô số (cộng được) và có định dạng tiền")
    void summaryCellsAreNumeric() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doanh Thu Ban Hang");
            CellStyle style = workbook.createCellStyle();

            SalesReportExcel.writeSheet(sheet, sample(), style, style);

            Row revenue = rowLabelled(sheet, "Doanh thu (VNĐ)");
            assertThat(revenue.getCell(1).getNumericCellValue()).isEqualTo(700000.0);
            assertThat(revenue.getCell(1).getCellStyle().getDataFormatString()).isEqualTo("#,##0");
            assertThat(rowLabelled(sheet, "Số đơn hoàn tất").getCell(1).getNumericCellValue()).isEqualTo(3.0);
            assertThat(rowLabelled(sheet, "Số cuốn đã bán").getCell(1).getNumericCellValue()).isEqualTo(4.0);
            assertThat(rowLabelled(sheet, "Giá trị đơn trung bình (VNĐ)").getCell(1).getNumericCellValue()).isEqualTo(233333.0);
            assertThat(rowLabelled(sheet, "Tổng giảm giá đã áp dụng (VNĐ)").getCell(1).getNumericCellValue()).isEqualTo(20000.0);
            assertThat(rowLabelled(sheet, "Tổng phí giao hàng thu (VNĐ)").getCell(1).getNumericCellValue()).isEqualTo(50000.0);
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("Sheet doanh thu: mỗi ngày một dòng kể cả ngày không bán được, tổng các ngày bằng tổng doanh thu")
    void dailyRowsCoverEveryDay() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doanh Thu Ban Hang");
            CellStyle style = workbook.createCellStyle();

            SalesReportExcel.writeSheet(sheet, sample(), style, style);

            int header = rowLabelled(sheet, "Ngày").getRowNum();
            assertThat(sheet.getRow(header + 1).getCell(0).getStringCellValue()).isEqualTo("01/10/2026");
            assertThat(sheet.getRow(header + 2).getCell(0).getStringCellValue()).isEqualTo("02/10/2026");
            assertThat(sheet.getRow(header + 2).getCell(2).getNumericCellValue()).isZero();
            double sum = 0;
            for (int i = 1; i <= 3; i++) {
                sum += sheet.getRow(header + i).getCell(2).getNumericCellValue();
            }
            assertThat(sum).isEqualTo(700000.0);
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("Sheet doanh thu: có bảng theo chi nhánh, theo hình thức thanh toán và sách bán chạy theo thứ tự")
    void breakdownTables() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doanh Thu Ban Hang");
            CellStyle style = workbook.createCellStyle();

            SalesReportExcel.writeSheet(sheet, sample(), style, style);

            Row ha = rowLabelled(sheet, "OldBook Ha Noi");
            assertThat(ha.getCell(1).getNumericCellValue()).isEqualTo(500000.0);
            assertThat(sheet.getRow(ha.getRowNum() + 1).getCell(0).getStringCellValue()).isEqualTo("OldBook Sai Gon");
            assertThat(rowLabelled(sheet, "Chuyển khoản").getCell(1).getNumericCellValue()).isEqualTo(450000.0);
            Row first = rowLabelled(sheet, "Hạng");
            assertThat(sheet.getRow(first.getRowNum() + 1).getCell(1).getStringCellValue()).isEqualTo("Trăm Năm Cô Đơn");
            assertThat(sheet.getRow(first.getRowNum() + 1).getCell(2).getNumericCellValue()).isEqualTo(3.0);
            assertThat(sheet.getRow(first.getRowNum() + 2).getCell(1).getStringCellValue()).isEqualTo("Nhà Giả Kim");
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("Mục tóm tắt ở sheet tổng quan ghi 4 dòng và trả về dòng kế tiếp")
    void overviewSection() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tong Quan");

            int next = SalesReportExcel.writeOverviewSection(sheet, 20, sample());

            assertThat(next).isEqualTo(25);
            assertThat(sheet.getRow(20).getCell(0).getStringCellValue()).contains("DOANH THU BÁN HÀNG ONLINE");
            assertThat(sheet.getRow(21).getCell(0).getStringCellValue()).endsWith("700000");
            assertThat(sheet.getRow(22).getCell(0).getStringCellValue()).endsWith("3");
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }
}
