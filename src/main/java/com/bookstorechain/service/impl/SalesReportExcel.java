package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.SalesReportDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Phần doanh thu bán hàng online trong file Excel báo cáo: mục tóm tắt ở sheet tổng quan và một sheet chi tiết riêng. */
final class SalesReportExcel {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private SalesReportExcel() {
    }

    /** Ghi mục "doanh thu" vào sheet tổng quan (cùng kiểu một dòng chữ như các mục khác); trả về dòng trống kế tiếp. */
    static int writeOverviewSection(Sheet sheet, int row, SalesReportDTO sales) {
        sheet.createRow(row++).createCell(0).setCellValue("=== 4. DOANH THU BÁN HÀNG ONLINE ===");
        sheet.createRow(row++).createCell(0).setCellValue("Doanh thu đơn đã hoàn tất (VNĐ): " + sales.getRevenue().toPlainString());
        sheet.createRow(row++).createCell(0).setCellValue("Số đơn hoàn tất: " + sales.getOrderCount());
        sheet.createRow(row++).createCell(0).setCellValue("Số cuốn đã bán: " + sales.getBooksSold());
        sheet.createRow(row++).createCell(0).setCellValue("Giá trị đơn trung bình (VNĐ): " + sales.getAverageOrderValue().toPlainString());
        return row;
    }

    /** Sheet chi tiết: tổng hợp, doanh thu từng ngày, theo chi nhánh, theo hình thức thanh toán, sách bán chạy. Tiền là ô số để cộng được. */
    static void writeSheet(Sheet sheet, SalesReportDTO sales, CellStyle titleStyle, CellStyle headerStyle) {
        CellStyle money = moneyStyle(sheet);
        int r = 0;

        Cell title = sheet.createRow(r++).createCell(0);
        title.setCellValue("DOANH THU BÁN HÀNG ONLINE — OLDBOOK");
        title.setCellStyle(titleStyle);
        sheet.createRow(r++).createCell(0).setCellValue("Từ ngày " + sales.getStartDate().format(DF) + " đến ngày " + sales.getEndDate().format(DF)
                + " | Chỉ tính đơn đã hoàn tất, theo ngày hoàn tất; doanh thu là số tiền khách thanh toán (đã trừ giảm giá, đã gồm phí giao hàng)");
        r++;

        r = pair(sheet, r, "Doanh thu (VNĐ)", sales.getRevenue(), money);
        r = pair(sheet, r, "Số đơn hoàn tất", BigDecimal.valueOf(sales.getOrderCount()), null);
        r = pair(sheet, r, "Số cuốn đã bán", BigDecimal.valueOf(sales.getBooksSold()), null);
        r = pair(sheet, r, "Giá trị đơn trung bình (VNĐ)", sales.getAverageOrderValue(), money);
        r = pair(sheet, r, "Tổng giảm giá đã áp dụng (VNĐ)", sales.getDiscountTotal(), money);
        r = pair(sheet, r, "Tổng phí giao hàng thu (VNĐ)", sales.getShippingTotal(), money);
        r++;

        r = header(sheet, r, headerStyle, "Ngày", "Số đơn", "Doanh thu (VNĐ)");
        for (SalesReportDTO.DailyRevenue day : sales.getDaily()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(day.getDate().format(DF));
            row.createCell(1).setCellValue(day.getOrders());
            number(row, 2, day.getRevenue(), money);
        }
        r++;

        r = breakdown(sheet, r, headerStyle, money, "Chi nhánh", sales.getRevenueByStore());
        r++;
        r = breakdown(sheet, r, headerStyle, money, "Hình thức thanh toán", sales.getRevenueByPayment());
        r++;

        r = header(sheet, r, headerStyle, "Hạng", "Tên sách bán chạy", "Số cuốn", "Doanh thu (VNĐ)");
        int rank = 1;
        for (SalesReportDTO.TopBook book : sales.getTopBooks()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(book.getTitle());
            row.createCell(2).setCellValue(book.getCopies());
            number(row, 3, book.getRevenue(), money);
        }

        sheet.setColumnWidth(0, 9000);
        sheet.setColumnWidth(1, 9000);
        sheet.setColumnWidth(2, 5500);
        sheet.setColumnWidth(3, 5500);
    }

    private static int pair(Sheet sheet, int r, String label, BigDecimal value, CellStyle style) {
        Row row = sheet.createRow(r);
        row.createCell(0).setCellValue(label);
        number(row, 1, value, style);
        return r + 1;
    }

    private static int breakdown(Sheet sheet, int r, CellStyle headerStyle, CellStyle money, String firstColumn, Map<String, BigDecimal> values) {
        r = header(sheet, r, headerStyle, firstColumn, "Doanh thu (VNĐ)");
        for (Map.Entry<String, BigDecimal> entry : values.entrySet()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(entry.getKey());
            number(row, 1, entry.getValue(), money);
        }
        return r;
    }

    private static int header(Sheet sheet, int r, CellStyle headerStyle, String... columns) {
        Row row = sheet.createRow(r);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
        return r + 1;
    }

    private static void number(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value.doubleValue());
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private static CellStyle moneyStyle(Sheet sheet) {
        CellStyle style = sheet.getWorkbook().createCellStyle();
        DataFormat format = sheet.getWorkbook().createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
}
