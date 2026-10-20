package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.StockTransferRequestDTO;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.TransferStatus;
import com.bookstorechain.service.BookCopyService;
import com.bookstorechain.service.StockTransferService;
import com.bookstorechain.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class StockTransferController {

    private final StockTransferService transferService;
    private final StoreService storeService;
    private final BookCopyService bookCopyService;

    @GetMapping
    public String listTransfers(@RequestParam(value = "storeId", required = false) Long storeId,
                                @RequestParam(value = "status", required = false) TransferStatus status,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                Model model) {
        model.addAttribute("transferPage", transferService.getTransfers(storeId, status, PageRequest.of(page, 10, Sort.by("id").descending())));
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("statuses", TransferStatus.values());
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedStatus", status);
        return "admin/transfer/transfer-list";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(value = "senderStoreId", required = false) Long senderStoreId, Model model) {
        model.addAttribute("transferForm", new StockTransferRequestDTO());
        model.addAttribute("stores", storeService.getAllStores());
        
        // Nếu chọn chi nhánh gửi, load danh sách BookCopy AVAILABLE của chi nhánh đó để chọn
        if (senderStoreId != null) {
            model.addAttribute("availableCopies", bookCopyService.getBookCopies(senderStoreId, CopyStatus.AVAILABLE, null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("selectedSenderId", senderStoreId);
        }
        return "admin/transfer/transfer-form";
    }

    @PostMapping("/new")
    public String createTransfer(@Valid @ModelAttribute("transferForm") StockTransferRequestDTO requestDTO,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("stores", storeService.getAllStores());
            if (requestDTO.getSenderStoreId() != null) {
                model.addAttribute("availableCopies", bookCopyService.getBookCopies(requestDTO.getSenderStoreId(), CopyStatus.AVAILABLE, null, PageRequest.of(0, 100)).getContent());
                model.addAttribute("selectedSenderId", requestDTO.getSenderStoreId());
            }
            return "admin/transfer/transfer-form";
        }

        try {
            transferService.createTransfer(requestDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu chuyển kho thành công!");
            return "redirect:/admin/transfers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("stores", storeService.getAllStores());
            if (requestDTO.getSenderStoreId() != null) {
                model.addAttribute("availableCopies", bookCopyService.getBookCopies(requestDTO.getSenderStoreId(), CopyStatus.AVAILABLE, null, PageRequest.of(0, 100)).getContent());
                model.addAttribute("selectedSenderId", requestDTO.getSenderStoreId());
            }
            return "admin/transfer/transfer-form";
        }
    }

    @GetMapping("/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        StockTransfer transfer = transferService.getTransferById(id);
        model.addAttribute("transfer", transfer);
        model.addAttribute("statuses", TransferStatus.values());
        return "admin/transfer/transfer-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("status") TransferStatus status,
                               @org.springframework.security.core.annotation.AuthenticationPrincipal com.bookstorechain.security.CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            StockTransfer transfer = transferService.getTransferById(id);
            if (userDetails != null && userDetails.getUser().getRole() == com.bookstorechain.enums.Role.STAFF) {
                Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
                boolean isRelated = staffStoreId != null &&
                        (staffStoreId.equals(transfer.getSenderStore().getId()) || staffStoreId.equals(transfer.getReceiverStore().getId()));
                if (!isRelated) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên chỉ có quyền cập nhật phiếu chuyển kho của chi nhánh mình phụ trách!");
                    return "redirect:/admin/transfers/" + id;
                }
            }

            transferService.updateTransferStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái phiếu chuyển thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/transfers/" + id;
    }
}