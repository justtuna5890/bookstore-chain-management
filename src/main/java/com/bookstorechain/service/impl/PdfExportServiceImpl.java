package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ReportSummaryDTO;
import com.bookstorechain.entity.BuybackItem;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.entity.StockTransferItem;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StockTransferRepository;
import com.bookstorechain.service.PdfExportService;
import com.bookstorechain.service.ReportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {

    private final StockTransferRepository stockTransferRepository;
    private final BuybackRequestRepository buybackRequestRepository;
    private final ReportService reportService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void exportTransferDetail(Long transferId, HttpServletResponse response) throws IOException {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu chuyen kho ID: " + transferId));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"phieu-chuyen-kho-" + transferId + ".pdf\"");

        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        titleFont.setColor(Color.decode("#1a3c5e"));
        Paragraph title = new Paragraph("PHIEU CHUYEN KHO GIUA CHI NHANH", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Paragraph sub = new Paragraph("Ma phieu: #" + transfer.getId() + "   |   Ngay tao: "
                + (transfer.getCreatedAt() != null ? transfer.getCreatedAt().format(DTF) : ""), subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);
        doc.add(new Paragraph(" "));

        // Info table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10f);

        addInfoRow(infoTable, "Chi nhanh gui:", transfer.getSenderStore() != null ? transfer.getSenderStore().getName() : "");
        addInfoRow(infoTable, "Chi nhanh nhan:", transfer.getReceiverStore() != null ? transfer.getReceiverStore().getName() : "");
        addInfoRow(infoTable, "Nguoi lap phieu:", transfer.getCreator() != null
                ? transfer.getCreator().getFullName() + " (" + transfer.getCreator().getUsername() + ")" : "");
        addInfoRow(infoTable, "Trang thai:", transfer.getStatus() != null ? transfer.getStatus().name() : "");
        addInfoRow(infoTable, "Ghi chu:", transfer.getNote() != null ? transfer.getNote() : "Khong co");
        doc.add(infoTable);
        doc.add(new Paragraph(" "));

        // Items table
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        doc.add(new Paragraph("Danh sach ban sach trong phieu:", sectionFont));
        doc.add(new Paragraph(" "));

        PdfPTable itemTable = new PdfPTable(5);
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[]{0.5f, 2f, 2f, 1.2f, 1.3f});

        String[] headers = {"#", "Ma Ban Sach", "Tua Sach", "Tinh Trang", "Trang Thai"};
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(Color.decode("#1a3c5e"));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemTable.addCell(cell);
        }

        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        int idx = 1;
        for (StockTransferItem item : transfer.getItems()) {
            itemTable.addCell(createDataCell(String.valueOf(idx++), dataFont));
            itemTable.addCell(createDataCell(item.getBookCopy() != null ? item.getBookCopy().getCopyCode() : "", dataFont));
            itemTable.addCell(createDataCell(item.getBookCopy() != null && item.getBookCopy().getBook() != null
                    ? item.getBookCopy().getBook().getTitle() : "", dataFont));
            itemTable.addCell(createDataCell(item.getBookCopy() != null && item.getBookCopy().getBookCondition() != null
                    ? item.getBookCopy().getBookCondition().name() : "", dataFont));
            itemTable.addCell(createDataCell(item.getBookCopy() != null && item.getBookCopy().getStatus() != null
                    ? item.getBookCopy().getStatus().name() : "", dataFont));
        }
        doc.add(itemTable);

        // Footer
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
        Paragraph footer = new Paragraph("Tai lieu duoc tao tu dong boi he thong Bookstore Chain Management", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
    }

    @Override
    public void exportBuybackDetail(Long buybackId, HttpServletResponse response) throws IOException {
        BuybackRequest br = buybackRequestRepository.findById(buybackId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay don thu mua ID: " + buybackId));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"phieu-thu-mua-" + buybackId + ".pdf\"");

        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        titleFont.setColor(Color.decode("#5a1e00"));
        Paragraph title = new Paragraph("PHIEU THU MUA SACH CU", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Paragraph sub = new Paragraph("Ma don: #" + br.getId() + "   |   Ngay gui: "
                + (br.getCreatedAt() != null ? br.getCreatedAt().format(DTF) : ""), subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);
        doc.add(new Paragraph(" "));

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10f);

        addInfoRow(infoTable, "Khach hang:", br.getCustomer() != null
                ? br.getCustomer().getFullName() + " (" + br.getCustomer().getEmail() + ")" : "");
        addInfoRow(infoTable, "Chi nhanh tiep nhan:", br.getStore() != null ? br.getStore().getName() : "");
        addInfoRow(infoTable, "Trang thai don:", br.getStatus() != null ? br.getStatus().name() : "");
        addInfoRow(infoTable, "Ghi chu nhan vien:", br.getStaffNote() != null ? br.getStaffNote() : "Chua co");
        doc.add(infoTable);
        doc.add(new Paragraph(" "));

        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        doc.add(new Paragraph("Danh sach sach khach gui ban:", sectionFont));
        doc.add(new Paragraph(" "));

        PdfPTable itemTable = new PdfPTable(5);
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1.5f, 1.5f});

        String[] headers = {"#", "Tua Sach", "Tac Gia", "Tinh Trang", "Gia De Xuat (VND)"};
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(Color.decode("#5a1e00"));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemTable.addCell(cell);
        }

        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        int idx = 1;
        for (BuybackItem item : br.getItems()) {
            itemTable.addCell(createDataCell(String.valueOf(idx++), dataFont));
            itemTable.addCell(createDataCell(item.getBookTitle() != null ? item.getBookTitle() : "", dataFont));
            itemTable.addCell(createDataCell(item.getBookAuthor() != null ? item.getBookAuthor() : "", dataFont));
            itemTable.addCell(createDataCell(item.getProposedCondition() != null ? item.getProposedCondition().name() : "", dataFont));
            itemTable.addCell(createDataCell(item.getOfferedPrice() != null
                    ? String.format("%,.0f", item.getOfferedPrice()) : "Chua dinh gia", dataFont));
        }
        doc.add(itemTable);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
        Paragraph footer = new Paragraph("Tai lieu duoc tao tu dong boi he thong Bookstore Chain Management", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
    }

    @Override
    public void exportPeriodicReportPdf(LocalDate startDate, LocalDate endDate, Long storeId, HttpServletResponse response) throws Exception {
        ReportSummaryDTO summary = reportService.generateReport(startDate, endDate, storeId);

        response.setContentType("application/pdf");
        String filename = "Bao-Cao-Van-Hanh-" + startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + endDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        Document doc = new Document(PageSize.A4, 32, 32, 36, 32);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        // 1. Header Title & Subtitle
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
        titleFont.setColor(Color.decode("#0f172a"));
        Paragraph title = new Paragraph("BAO CAO TONG HOP HOAT DONG VAN HANH & KHO SACH", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f);
        brandFont.setColor(Color.decode("#b45309"));
        Paragraph brand = new Paragraph("HE THONG CHUOI CUA HANG SACH CU — OLDBOOK", brandFont);
        brand.setAlignment(Element.ALIGN_CENTER);
        doc.add(brand);

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        String metaText = "Pham vi: " + summary.getStoreName() +
                "   |   Tu ngay: " + summary.getStartDate().format(DF) +
                " Den ngay: " + summary.getEndDate().format(DF) +
                "   |   Xuat luc: " + LocalDateTime.now().format(DTF);
        Paragraph meta = new Paragraph(metaText, metaFont);
        meta.setAlignment(Element.ALIGN_CENTER);
        doc.add(meta);
        doc.add(new Paragraph(" "));

        // 2. Section 1: KPI Overview Table
        Font secFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Color.decode("#1e293b"));
        doc.add(new Paragraph("I. TONG HOP CHI SO VAN HANH TRONG KY", secFont));
        doc.add(new Paragraph(" "));

        PdfPTable kpiTable = new PdfPTable(4);
        kpiTable.setWidthPercentage(100);
        kpiTable.setWidths(new float[]{1.6f, 1.4f, 1.6f, 1.4f});

        addKpiCell(kpiTable, "Tong don thu mua:", String.valueOf(summary.getTotalBuybacks()), false);
        addKpiCell(kpiTable, "Don thu mua hoan tat:", String.valueOf(summary.getCompletedBuybacks()), false);

        addKpiCell(kpiTable, "Don cho tham dinh:", String.valueOf(summary.getPendingBuybacks()), false);
        addKpiCell(kpiTable, "Don bi tu choi:", String.valueOf(summary.getRejectedBuybacks()), false);

        addKpiCell(kpiTable, "Ty le duyet thanh cong:", summary.getBuybackApprovalRate() + "%", true);
        addKpiCell(kpiTable, "Chi phi thu mua giai ngan:", String.format("%,.0f VND", summary.getTotalBuybackAmount()), true);

        addKpiCell(kpiTable, "Tong so phieu chuyen kho:", String.valueOf(summary.getTotalTransfers()), false);
        addKpiCell(kpiTable, "Phieu da hoan tat nhan:", String.valueOf(summary.getCompletedTransfers()), false);

        addKpiCell(kpiTable, "Phieu dang van chuyen:", String.valueOf(summary.getInTransitTransfers()), false);
        addKpiCell(kpiTable, "So ban sach luan chuyen:", summary.getTotalCopiesTransferred() + " cuon", false);

        addKpiCell(kpiTable, "Tong ban sach ton kho:", summary.getTotalBookCopies() + " cuon", true);
        addKpiCell(kpiTable, "Gia tri kho uoc tinh:", String.format("%,.0f VND", summary.getTotalInventoryValue()), true);

        addKpiCell(kpiTable, "Tong khach hang dang ky:", String.valueOf(summary.getTotalCustomers()), false);
        addKpiCell(kpiTable, "Khach hang moi trong ky:", String.valueOf(summary.getNewCustomersInPeriod()), false);

        doc.add(kpiTable);
        doc.add(new Paragraph(" "));

        // 3. Section 2: Recent Buyback Table
        doc.add(new Paragraph("II. DANH SACH HO SO THU MUA SACH CU (TRONG KY BAO CAO)", secFont));
        doc.add(new Paragraph(" "));

        PdfPTable bbTable = new PdfPTable(6);
        bbTable.setWidthPercentage(100);
        bbTable.setWidths(new float[]{0.9f, 1.8f, 2.0f, 0.8f, 1.5f, 1.2f});

        String[] bbHeaders = {"Ma Don", "Chi Nhanh", "Khach Hang", "So Cuon", "Tong Tien (VND)", "Trang Thai"};
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.WHITE);
        for (String h : bbHeaders) {
            PdfPCell c = new PdfPCell(new Phrase(h, thFont));
            c.setBackgroundColor(Color.decode("#b45309")); // amber
            c.setPadding(3.5f);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            bbTable.addCell(c);
        }

        Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f);
        int bbCount = 0;
        for (BuybackRequest br : summary.getBuybackRequests()) {
            if (bbCount++ >= 12) break;
            BigDecimal amt = BigDecimal.ZERO;
            if (br.getItems() != null) {
                for (BuybackItem item : br.getItems()) {
                    if (item.getOfferedPrice() != null) amt = amt.add(item.getOfferedPrice());
                }
            }
            bbTable.addCell(createDataCell("#BB-" + br.getId(), tdFont));
            bbTable.addCell(createDataCell(br.getStore() != null ? br.getStore().getName() : "", tdFont));
            bbTable.addCell(createDataCell(br.getCustomer() != null ? br.getCustomer().getFullName() : "", tdFont));
            bbTable.addCell(createDataCell(String.valueOf(br.getItems() != null ? br.getItems().size() : 0), tdFont));
            bbTable.addCell(createDataCell(String.format("%,.0f", amt), tdFont));
            bbTable.addCell(createDataCell(br.getStatus() != null ? br.getStatus().name() : "", tdFont));
        }
        if (summary.getBuybackRequests().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Khong co yeu cau thu mua nao trong ky bao cao", tdFont));
            empty.setColspan(6);
            empty.setPadding(5);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            bbTable.addCell(empty);
        }
        doc.add(bbTable);
        doc.add(new Paragraph(" "));

        // 4. Section 3: Recent Stock Transfers
        doc.add(new Paragraph("III. DANH SACH PHIEU DIEU CHUYEN KHO LIEN CHI NHANH", secFont));
        doc.add(new Paragraph(" "));

        PdfPTable trTable = new PdfPTable(6);
        trTable.setWidthPercentage(100);
        trTable.setWidths(new float[]{0.9f, 1.8f, 1.8f, 1.5f, 0.8f, 1.2f});

        String[] trHeaders = {"Ma Phieu", "Chi Nhanh Gui", "Chi Nhanh Nhan", "Nguoi Lap", "So Cuon", "Trang Thai"};
        for (String h : trHeaders) {
            PdfPCell c = new PdfPCell(new Phrase(h, thFont));
            c.setBackgroundColor(Color.decode("#1e3a8a")); // blue
            c.setPadding(3.5f);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            trTable.addCell(c);
        }

        int trCount = 0;
        for (StockTransfer st : summary.getStockTransfers()) {
            if (trCount++ >= 12) break;
            trTable.addCell(createDataCell("#TR-" + st.getId(), tdFont));
            trTable.addCell(createDataCell(st.getSenderStore() != null ? st.getSenderStore().getName() : "", tdFont));
            trTable.addCell(createDataCell(st.getReceiverStore() != null ? st.getReceiverStore().getName() : "", tdFont));
            trTable.addCell(createDataCell(st.getCreator() != null ? st.getCreator().getFullName() : "", tdFont));
            trTable.addCell(createDataCell(String.valueOf(st.getItems() != null ? st.getItems().size() : 0), tdFont));
            trTable.addCell(createDataCell(st.getStatus() != null ? st.getStatus().name() : "", tdFont));
        }
        if (summary.getStockTransfers().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Khong co phieu dieu chuyen kho nao trong ky bao cao", tdFont));
            empty.setColspan(6);
            empty.setPadding(5);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            trTable.addCell(empty);
        }
        doc.add(trTable);
        doc.add(new Paragraph(" "));

        // 5. Signatures Block
        PdfPTable sigTable = new PdfPTable(3);
        sigTable.setWidthPercentage(100);
        sigTable.setSpacingBefore(12f);

        Font sigTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.decode("#1e293b"));
        Font sigNoteFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7.5f, Color.GRAY);

        PdfPCell c1 = new PdfPCell();
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.addElement(new Paragraph("NGUOI LAP BAO CAO", sigTitleFont));
        c1.addElement(new Paragraph("(Ky va ghi ro ho ten)", sigNoteFont));
        sigTable.addCell(c1);

        PdfPCell c2 = new PdfPCell();
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.addElement(new Paragraph("QUAN LY CHI NHANH / VUNG", sigTitleFont));
        c2.addElement(new Paragraph("(Ky va xac nhan)", sigNoteFont));
        sigTable.addCell(c2);

        PdfPCell c3 = new PdfPCell();
        c3.setBorder(Rectangle.NO_BORDER);
        c3.setHorizontalAlignment(Element.ALIGN_CENTER);
        c3.addElement(new Paragraph("BAN GIAM DOC HE THONG", sigTitleFont));
        c3.addElement(new Paragraph("(Phe duyet luu tru)", sigNoteFont));
        sigTable.addCell(c3);

        doc.add(sigTable);

        doc.close();
    }

    private void addKpiCell(PdfPTable table, String label, String value, boolean highlight) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.GRAY);
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f,
                highlight ? Color.decode("#b45309") : Color.decode("#0f172a"));

        PdfPCell cell = new PdfPCell();
        cell.setPadding(4f);
        cell.setBackgroundColor(highlight ? Color.decode("#fef3c7") : Color.decode("#f8fafc"));
        cell.setBorderColor(Color.decode("#e2e8f0"));

        Paragraph p = new Paragraph(label + " ", labelFont);
        p.add(new Chunk(value, valFont));
        cell.addElement(p);

        table.addCell(cell);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        labelCell.setBackgroundColor(Color.decode("#f5f5f5"));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private PdfPCell createDataCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3.5f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
}
