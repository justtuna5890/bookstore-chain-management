package com.bookstorechain.service.impl;

import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StockTransferRepository;
import com.bookstorechain.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private final BookCopyRepository bookCopyRepository;
    private final StockTransferRepository stockTransferRepository;
    private final BuybackRequestRepository buybackRequestRepository;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void exportInventory(Long storeId, HttpServletResponse response) throws IOException {
        List<BookCopy> copies = bookCopyRepository
                .searchBookCopies(storeId, null, null, PageRequest.of(0, 10000))
                .getContent();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"ton-kho.xlsx\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ton Kho");

            // Header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header row
            Row header = sheet.createRow(0);
            String[] cols = {"#", "Ma Ban Sach", "Tua Sach", "Tac Gia", "Chi Nhanh", "Tinh Trang", "Trang Thai", "Gia Ban (VND)", "Ngay Them"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Data rows
            int rowNum = 1;
            for (BookCopy bc : copies) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(bc.getCopyCode());
                row.createCell(2).setCellValue(bc.getBook() != null ? bc.getBook().getTitle() : "");
                row.createCell(3).setCellValue(bc.getBook() != null ? bc.getBook().getAuthor() : "");
                row.createCell(4).setCellValue(bc.getStore() != null ? bc.getStore().getName() : "");
                row.createCell(5).setCellValue(bc.getBookCondition() != null ? bc.getBookCondition().name() : "");
                row.createCell(6).setCellValue(bc.getStatus() != null ? bc.getStatus().name() : "");
                row.createCell(7).setCellValue(bc.getPrice() != null ? bc.getPrice().doubleValue() : 0);
                row.createCell(8).setCellValue(bc.getCreatedAt() != null ? bc.getCreatedAt().format(DTF) : "");
            }

            workbook.write(response.getOutputStream());
        }
    }

    @Override
    public void exportTransfers(HttpServletResponse response) throws IOException {
        List<StockTransfer> transfers = stockTransferRepository
                .searchTransfers(null, null, PageRequest.of(0, 10000))
                .getContent();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"chuyen-kho.xlsx\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Chuyen Kho");
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row header = sheet.createRow(0);
            String[] cols = {"Ma Phieu", "Chi Nhanh Gui", "Chi Nhanh Nhan", "Nguoi Tao", "Trang Thai", "So Ban Sach", "Ghi Chu", "Ngay Tao"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }

            int rowNum = 1;
            for (StockTransfer t : transfers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getSenderStore() != null ? t.getSenderStore().getName() : "");
                row.createCell(2).setCellValue(t.getReceiverStore() != null ? t.getReceiverStore().getName() : "");
                row.createCell(3).setCellValue(t.getCreator() != null ? t.getCreator().getUsername() : "");
                row.createCell(4).setCellValue(t.getStatus() != null ? t.getStatus().name() : "");
                row.createCell(5).setCellValue(t.getItems() != null ? t.getItems().size() : 0);
                row.createCell(6).setCellValue(t.getNote() != null ? t.getNote() : "");
                row.createCell(7).setCellValue(t.getCreatedAt() != null ? t.getCreatedAt().format(DTF) : "");
            }

            workbook.write(response.getOutputStream());
        }
    }

    @Override
    public void exportBuybacks(HttpServletResponse response) throws IOException {
        List<BuybackRequest> requests = buybackRequestRepository
                .searchRequests(null, null, PageRequest.of(0, 10000))
                .getContent();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"thu-mua.xlsx\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thu Mua");
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row header = sheet.createRow(0);
            String[] cols = {"Ma Don", "Khach Hang", "Email", "Chi Nhanh Tiep Nhan", "Trang Thai", "So Quyen", "Ghi Chu NV", "Ngay Gui"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }

            int rowNum = 1;
            for (BuybackRequest br : requests) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(br.getId());
                row.createCell(1).setCellValue(br.getCustomer() != null ? br.getCustomer().getFullName() : "");
                row.createCell(2).setCellValue(br.getCustomer() != null ? br.getCustomer().getEmail() : "");
                row.createCell(3).setCellValue(br.getStore() != null ? br.getStore().getName() : "");
                row.createCell(4).setCellValue(br.getStatus() != null ? br.getStatus().name() : "");
                row.createCell(5).setCellValue(br.getItems() != null ? br.getItems().size() : 0);
                row.createCell(6).setCellValue(br.getStaffNote() != null ? br.getStaffNote() : "");
                row.createCell(7).setCellValue(br.getCreatedAt() != null ? br.getCreatedAt().format(DTF) : "");
            }

            workbook.write(response.getOutputStream());
        }
    }

    /** Tạo cell style cho header row */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }
}
