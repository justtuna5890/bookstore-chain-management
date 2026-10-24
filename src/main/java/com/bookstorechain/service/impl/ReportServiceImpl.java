package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ReportSummaryDTO;
import com.bookstorechain.dto.response.SalesReportDTO;
import com.bookstorechain.entity.*;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.enums.TransferStatus;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StockTransferRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.DashboardService;
import com.bookstorechain.service.ReportService;
import com.bookstorechain.service.SalesStatsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final BuybackRequestRepository buybackRequestRepository;
    private final StockTransferRepository stockTransferRepository;
    private final StoreRepository storeRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final DashboardService dashboardService;
    private final SalesStatsService salesStatsService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public ReportSummaryDTO generateReport(LocalDate startDate, LocalDate endDate, Long storeId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<BuybackRequest> buybacks = buybackRequestRepository.findForReport(storeId, startDateTime, endDateTime);
        List<StockTransfer> transfers = stockTransferRepository.findForReport(storeId, startDateTime, endDateTime);

        long completedBuybacks = 0;
        long pendingBuybacks = 0;
        long rejectedBuybacks = 0;
        BigDecimal totalBuybackAmount = BigDecimal.ZERO;

        // Bảng tổng hợp hoạt động khách hàng trong kỳ
        Map<Long, ReportSummaryDTO.CustomerActivityDTO> custMap = new LinkedHashMap<>();

        for (BuybackRequest br : buybacks) {
            BigDecimal brAmount = BigDecimal.ZERO;
            if (br.getItems() != null) {
                for (BuybackItem item : br.getItems()) {
                    if (item.getOfferedPrice() != null) {
                        brAmount = brAmount.add(item.getOfferedPrice());
                    }
                }
            }

            if (br.getStatus() == BuybackStatus.COMPLETED) {
                completedBuybacks++;
                totalBuybackAmount = totalBuybackAmount.add(brAmount);
            } else if (br.getStatus() == BuybackStatus.PENDING || br.getStatus() == BuybackStatus.APPROVED) {
                pendingBuybacks++;
            } else if (br.getStatus() == BuybackStatus.REJECTED) {
                rejectedBuybacks++;
            }

            if (br.getCustomer() != null) {
                User c = br.getCustomer();
                ReportSummaryDTO.CustomerActivityDTO dto = custMap.computeIfAbsent(c.getId(), k ->
                        ReportSummaryDTO.CustomerActivityDTO.builder()
                                .customerId(c.getId())
                                .fullName(c.getFullName() != null ? c.getFullName() : c.getUsername())
                                .email(c.getEmail())
                                .phone(c.getPhone() != null ? c.getPhone() : "Chưa cập nhật")
                                .totalRequests(0)
                                .completedRequests(0)
                                .totalPayout(BigDecimal.ZERO)
                                .build()
                );
                dto.setTotalRequests(dto.getTotalRequests() + 1);
                if (br.getStatus() == BuybackStatus.COMPLETED) {
                    dto.setCompletedRequests(dto.getCompletedRequests() + 1);
                    dto.setTotalPayout(dto.getTotalPayout().add(brAmount));
                }
            }
        }

        long resolvedBuybacks = completedBuybacks + rejectedBuybacks;
        double buybackApprovalRate = resolvedBuybacks > 0
                ? Math.round(((double) completedBuybacks / resolvedBuybacks) * 1000.0) / 10.0
                : 0.0;

        List<ReportSummaryDTO.CustomerActivityDTO> customerActivities = new ArrayList<>(custMap.values());
        customerActivities.sort((a, b) -> Long.compare(b.getTotalRequests(), a.getTotalRequests()));

        long completedTransfers = 0;
        long inTransitTransfers = 0;
        long pendingTransfers = 0;
        long cancelledTransfers = 0;
        long totalCopiesTransferred = 0;
        Map<String, Long> transferFlowMatrix = new LinkedHashMap<>();

        for (StockTransfer st : transfers) {
            int itemCount = st.getItems() != null ? st.getItems().size() : 0;
            if (st.getStatus() == TransferStatus.COMPLETED) {
                completedTransfers++;
                totalCopiesTransferred += itemCount;
                if (st.getSenderStore() != null && st.getReceiverStore() != null) {
                    String route = st.getSenderStore().getName() + " ➔ " + st.getReceiverStore().getName();
                    transferFlowMatrix.put(route, transferFlowMatrix.getOrDefault(route, 0L) + 1L);
                }
            } else if (st.getStatus() == TransferStatus.IN_TRANSIT) {
                inTransitTransfers++;
                totalCopiesTransferred += itemCount;
            } else if (st.getStatus() == TransferStatus.PENDING) {
                pendingTransfers++;
            } else if (st.getStatus() == TransferStatus.CANCELLED) {
                cancelledTransfers++;
            }
        }

        String storeName = "Toàn Hệ Thống";
        long totalCopies;
        BigDecimal totalInventoryVal = BigDecimal.ZERO;
        Map<String, BigDecimal> storeValues = dashboardService.getInventoryValueByStore();

        if (storeId != null) {
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                storeName = store.getName();
            }
            totalCopies = bookCopyRepository.countByStoreId(storeId);
            totalInventoryVal = storeValues.getOrDefault(storeName, BigDecimal.ZERO);
        } else {
            totalCopies = bookCopyRepository.countInStock();
            for (BigDecimal val : storeValues.values()) {
                if (val != null) {
                    totalInventoryVal = totalInventoryVal.add(val);
                }
            }
        }

        // Tồn kho chi tiết theo tình trạng và trạng thái
        Map<String, Long> copiesByCondition = new LinkedHashMap<>();
        List<Object[]> conditionRows = bookCopyRepository.countByConditionAndOptionalStore(storeId);
        for (Object[] row : conditionRows) {
            String cond = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long cnt = ((Number) row[1]).longValue();
            copiesByCondition.put(cond, cnt);
        }

        Map<String, Long> copiesByStatus = new LinkedHashMap<>();
        List<Object[]> statusRows = bookCopyRepository.countByStatusAndStoreIdGrouped(storeId);
        for (Object[] row : statusRows) {
            String st = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long cnt = ((Number) row[1]).longValue();
            copiesByStatus.put(st, cnt);
        }

        List<Object[]> lowStockBooks = bookCopyRepository.findLowStockBooks(storeId);
        long staleCopiesCount = bookCopyRepository.countStaleCopies(storeId, LocalDateTime.now().minusDays(30));

        // Khách hàng
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long newCustomersInPeriod = userRepository.countByRoleAndCreatedAtBetween(Role.CUSTOMER, startDateTime, endDateTime);

        // Doanh thu bán hàng online cùng kỳ và cùng phạm vi chi nhánh; bỏ qua nếu khoảng thời gian quá dài
        SalesReportDTO sales = ChronoUnit.DAYS.between(startDate, endDate) + 1 > SalesStatsService.MAX_RANGE_DAYS
                ? null : salesStatsService.getReport(startDate, endDate, storeId);

        return ReportSummaryDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .storeId(storeId)
                .storeName(storeName)
                // Thu mua
                .totalBuybacks(buybacks.size())
                .completedBuybacks(completedBuybacks)
                .pendingBuybacks(pendingBuybacks)
                .rejectedBuybacks(rejectedBuybacks)
                .buybackApprovalRate(buybackApprovalRate)
                .totalBuybackAmount(totalBuybackAmount)
                .buybackRequests(buybacks)
                // Chuyển kho
                .totalTransfers(transfers.size())
                .completedTransfers(completedTransfers)
                .inTransitTransfers(inTransitTransfers)
                .pendingTransfers(pendingTransfers)
                .cancelledTransfers(cancelledTransfers)
                .totalCopiesTransferred(totalCopiesTransferred)
                .stockTransfers(transfers)
                .transferFlowMatrix(transferFlowMatrix)
                // Tồn kho
                .totalBookCopies(totalCopies)
                .totalInventoryValue(totalInventoryVal)
                .copiesByCondition(copiesByCondition)
                .copiesByStatus(copiesByStatus)
                .lowStockBooks(lowStockBooks)
                .staleCopiesCount(staleCopiesCount)
                // Khách hàng
                .totalCustomers(totalCustomers)
                .newCustomersInPeriod(newCustomersInPeriod)
                .customerActivities(customerActivities)
                // Doanh thu
                .sales(sales)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public void exportReportExcel(LocalDate startDate, LocalDate endDate, Long storeId, HttpServletResponse response) throws Exception {
        ReportSummaryDTO summary = generateReport(startDate, endDate, storeId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = "Bao-Cao-Van-Hanh-" + startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + endDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            // Sheet 1: Tổng quan
            Sheet sheetOverview = workbook.createSheet("Tong Quan Ky Bao Cao");
            createOverviewSheet(sheetOverview, summary, titleStyle, headerStyle);

            // Sheet 2: Danh sách thu mua
            Sheet sheetBuyback = workbook.createSheet("Thu Mua Sach Cu");
            createBuybackSheet(sheetBuyback, summary.getBuybackRequests(), headerStyle);

            // Sheet 3: Danh sách chuyển kho
            Sheet sheetTransfer = workbook.createSheet("Dieu Chuyen Kho");
            createTransferSheet(sheetTransfer, summary.getStockTransfers(), headerStyle);

            // Sheet 4: Doanh thu bán hàng online
            if (summary.getSales() != null) {
                SalesReportExcel.writeSheet(workbook.createSheet("Doanh Thu Ban Hang"), summary.getSales(), titleStyle, headerStyle);
            }

            workbook.write(response.getOutputStream());
        }
    }

    private void createOverviewSheet(Sheet sheet, ReportSummaryDTO summary, CellStyle titleStyle, CellStyle headerStyle) {
        Row row0 = sheet.createRow(0);
        Cell cell0 = row0.createCell(0);
        cell0.setCellValue("BÁO CÁO TỔNG HỢP VẬN HÀNH — OLDBOOK");
        cell0.setCellStyle(titleStyle);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("Phạm vi: " + summary.getStoreName() + " | Từ ngày: " + summary.getStartDate().format(DF) + " Đến ngày: " + summary.getEndDate().format(DF));

        int r = 3;
        sheet.createRow(r++).createCell(0).setCellValue("=== 1. CHỈ SỐ THU MUA SÁCH CŨ ===");
        sheet.createRow(r++).createCell(0).setCellValue("Tổng số đơn tiếp nhận: " + summary.getTotalBuybacks());
        sheet.createRow(r++).createCell(0).setCellValue("Đơn đã hoàn tất thu mua: " + summary.getCompletedBuybacks());
        sheet.createRow(r++).createCell(0).setCellValue("Đơn đang chờ thẩm định: " + summary.getPendingBuybacks());
        sheet.createRow(r++).createCell(0).setCellValue("Đơn đã hủy/từ chối: " + summary.getRejectedBuybacks());
        sheet.createRow(r++).createCell(0).setCellValue("Tỷ lệ duyệt thành công: " + summary.getBuybackApprovalRate() + "%");
        sheet.createRow(r++).createCell(0).setCellValue("Tổng chi phí thu mua giải ngân (VNĐ): " + summary.getTotalBuybackAmount());

        r++;
        sheet.createRow(r++).createCell(0).setCellValue("=== 2. CHỈ SỐ ĐIỀU CHUYỂN KHO ===");
        sheet.createRow(r++).createCell(0).setCellValue("Tổng số phiếu chuyển kho: " + summary.getTotalTransfers());
        sheet.createRow(r++).createCell(0).setCellValue("Phiếu đã hoàn tất: " + summary.getCompletedTransfers());
        sheet.createRow(r++).createCell(0).setCellValue("Phiếu đang luân chuyển: " + summary.getInTransitTransfers());
        sheet.createRow(r++).createCell(0).setCellValue("Tổng số bản sách đã chuyển: " + summary.getTotalCopiesTransferred());

        r++;
        sheet.createRow(r++).createCell(0).setCellValue("=== 3. TỒN KHO & ĐỘC GIẢ ===");
        sheet.createRow(r++).createCell(0).setCellValue("Tổng số cuốn sách trong kho: " + summary.getTotalBookCopies());
        sheet.createRow(r++).createCell(0).setCellValue("Giá trị kho ước tính (VNĐ): " + summary.getTotalInventoryValue());
        sheet.createRow(r++).createCell(0).setCellValue("Tổng số khách hàng đăng ký: " + summary.getTotalCustomers());
        sheet.createRow(r++).createCell(0).setCellValue("Khách hàng mới trong kỳ: " + summary.getNewCustomersInPeriod());

        if (summary.getSales() != null) {
            r++;
            r = SalesReportExcel.writeOverviewSection(sheet, r, summary.getSales());
        }

        sheet.setColumnWidth(0, 12000);
    }

    private void createBuybackSheet(Sheet sheet, List<BuybackRequest> buybacks, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        String[] cols = {"#", "Mã Đơn", "Chi Nhánh", "Khách Hàng", "SĐT", "Trạng Thái", "Số Cuốn", "Tổng Tiền (VNĐ)", "Ngày Gửi", "Ghi Chú Nhân Viên"};
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5500);
        }

        int rowNum = 1;
        for (BuybackRequest br : buybacks) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue("BB-" + br.getId());
            row.createCell(2).setCellValue(br.getStore() != null ? br.getStore().getName() : "");
            row.createCell(3).setCellValue(br.getCustomer() != null ? br.getCustomer().getFullName() : "");
            row.createCell(4).setCellValue(br.getCustomer() != null ? br.getCustomer().getPhone() : "");
            row.createCell(5).setCellValue(br.getStatus() != null ? br.getStatus().name() : "");
            row.createCell(6).setCellValue(br.getItems() != null ? br.getItems().size() : 0);

            BigDecimal amount = BigDecimal.ZERO;
            if (br.getItems() != null) {
                for (BuybackItem item : br.getItems()) {
                    if (item.getOfferedPrice() != null) {
                        amount = amount.add(item.getOfferedPrice());
                    }
                }
            }
            row.createCell(7).setCellValue(amount.doubleValue());
            row.createCell(8).setCellValue(br.getCreatedAt() != null ? br.getCreatedAt().format(DTF) : "");
            row.createCell(9).setCellValue(br.getStaffNote() != null ? br.getStaffNote() : "");
        }
    }

    private void createTransferSheet(Sheet sheet, List<StockTransfer> transfers, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        String[] cols = {"#", "Mã Phiếu", "Chi Nhánh Gửi", "Chi Nhánh Nhận", "Người Tạo", "Trạng Thái", "Số Cuốn Sách", "Ngày Lập", "Ghi Chú"};
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5500);
        }

        int rowNum = 1;
        for (StockTransfer st : transfers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue("TR-" + st.getId());
            row.createCell(2).setCellValue(st.getSenderStore() != null ? st.getSenderStore().getName() : "");
            row.createCell(3).setCellValue(st.getReceiverStore() != null ? st.getReceiverStore().getName() : "");
            row.createCell(4).setCellValue(st.getCreator() != null ? st.getCreator().getFullName() : "");
            row.createCell(5).setCellValue(st.getStatus() != null ? st.getStatus().name() : "");
            row.createCell(6).setCellValue(st.getItems() != null ? st.getItems().size() : 0);
            row.createCell(7).setCellValue(st.getCreatedAt() != null ? st.getCreatedAt().format(DTF) : "");
            row.createCell(8).setCellValue(st.getNote() != null ? st.getNote() : "");
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }
}
