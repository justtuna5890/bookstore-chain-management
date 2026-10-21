package com.bookstorechain.service.impl;

import com.bookstorechain.entity.BuybackItem;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.entity.StockTransferItem;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StockTransferRepository;
import com.bookstorechain.service.PdfExportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {

    private final StockTransferRepository stockTransferRepository;
    private final BuybackRequestRepository buybackRequestRepository;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
}
