package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.StockTransferRequestDTO;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.enums.TransferStatus;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.BookCopyService;
import com.bookstorechain.service.StockTransferService;
import com.bookstorechain.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        boolean isStaff = userDetails != null && userDetails.getUser().getRole() == Role.STAFF;

        if (isStaff) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                model.addAttribute("unassignedStoreWarning", true);
                model.addAttribute("transferPage", Page.empty());
                model.addAttribute("stores", List.of());
                model.addAttribute("statuses", TransferStatus.values());
                return "admin/transfer/transfer-list";
            }
            storeId = staffStoreId;
            model.addAttribute("isStaffScoped", true);
            model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
            model.addAttribute("stores", List.of(userDetails.getUser().getStore()));
        } else {
            model.addAttribute("stores", storeService.getAllStores());
        }

        model.addAttribute("transferPage", transferService.getTransfers(storeId, status, PageRequest.of(page, 10, Sort.by("id").descending())));
        model.addAttribute("statuses", TransferStatus.values());
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedStatus", status);
        return "admin/transfer/transfer-list";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(value = "senderStoreId", required = false) Long senderStoreId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        boolean isStaff = userDetails != null && userDetails.getUser().getRole() == Role.STAFF;
        StockTransferRequestDTO form = new StockTransferRequestDTO();

        if (isStaff) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản nhân viên chưa được gán chi nhánh để lập phiếu điều chuyển!");
                return "redirect:/admin/transfers";
            }
            senderStoreId = staffStoreId;
            form.setSenderStoreId(staffStoreId);
            model.addAttribute("isStaffScoped", true);
            model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
        }

        model.addAttribute("transferForm", form);
        model.addAttribute("stores", storeService.getAllStores());

        // Nếu xác định được chi nhánh gửi, nạp sách AVAILABLE của chi nhánh đó
        if (senderStoreId != null) {
            model.addAttribute("availableCopies", bookCopyService.getBookCopies(senderStoreId, CopyStatus.AVAILABLE, null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("selectedSenderId", senderStoreId);
        }
        return "admin/transfer/transfer-form";
    }

    @PostMapping("/new")
    public String createTransfer(@Valid @ModelAttribute("transferForm") StockTransferRequestDTO requestDTO,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        boolean isStaff = userDetails != null && userDetails.getUser().getRole() == Role.STAFF;

        if (isStaff) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            if (staffStoreId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản nhân viên chưa được gán chi nhánh!");
                return "redirect:/admin/transfers";
            }
            requestDTO.setSenderStoreId(staffStoreId);
        }

        if (requestDTO.getSenderStoreId() != null && requestDTO.getSenderStoreId().equals(requestDTO.getReceiverStoreId())) {
            bindingResult.rejectValue("receiverStoreId", "error.receiverStore", "Chi nhánh nhận phải khác với chi nhánh xuất sách!");
        }

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
    public String viewDetail(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        StockTransfer transfer = transferService.getTransferById(id);
        boolean isStaff = userDetails != null && userDetails.getUser().getRole() == Role.STAFF;

        if (isStaff) {
            Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
            boolean isRelated = staffStoreId != null &&
                    (staffStoreId.equals(transfer.getSenderStore().getId()) || staffStoreId.equals(transfer.getReceiverStore().getId()));
            if (!isRelated) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên chỉ có quyền xem phiếu chuyển kho của chi nhánh mình phụ trách!");
                return "redirect:/admin/transfers";
            }
            model.addAttribute("isStaffScoped", true);
            model.addAttribute("currentStoreName", userDetails.getUser().getStore().getName());
        }

        model.addAttribute("transfer", transfer);
        model.addAttribute("statuses", TransferStatus.values());
        return "admin/transfer/transfer-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("status") TransferStatus status,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            StockTransfer transfer = transferService.getTransferById(id);
            boolean isStaff = userDetails != null && userDetails.getUser().getRole() == Role.STAFF;

            if (isStaff) {
                Long staffStoreId = userDetails.getUser().getStore() != null ? userDetails.getUser().getStore().getId() : null;
                boolean isSender = staffStoreId != null && staffStoreId.equals(transfer.getSenderStore().getId());
                boolean isReceiver = staffStoreId != null && staffStoreId.equals(transfer.getReceiverStore().getId());

                if (!isSender && !isReceiver) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên chỉ có quyền cập nhật phiếu chuyển kho của chi nhánh mình phụ trách!");
                    return "redirect:/admin/transfers/" + id;
                }

                // 1. Phê duyệt xuất kho (PENDING -> IN_TRANSIT)
                if (status == TransferStatus.IN_TRANSIT) {
                    // Người lập phiếu không được tự duyệt phiếu của chính mình
                    if (transfer.getCreator() != null && transfer.getCreator().getUsername().equalsIgnoreCase(userDetails.getUsername())) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Người lập phiếu không được tự phê duyệt xuất kho cho chính phiếu của mình! Cần một nhân viên khác hoặc Quản lý phê duyệt.");
                        return "redirect:/admin/transfers/" + id;
                    }
                    if (!isSender) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Chỉ nhân viên chi nhánh gửi (" + transfer.getSenderStore().getName() + ") mới có thẩm quyền xuất kho vận chuyển!");
                        return "redirect:/admin/transfers/" + id;
                    }
                }

                // 2. Xác nhận tiếp nhận hoàn tất (IN_TRANSIT -> COMPLETED)
                if (status == TransferStatus.COMPLETED) {
                    if (!isReceiver) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Chỉ nhân viên chi nhánh nhận (" + transfer.getReceiverStore().getName() + ") mới có thẩm quyền xác nhận đã tiếp nhận sách!");
                        return "redirect:/admin/transfers/" + id;
                    }
                }

                // 3. Hủy phiếu (CANCELLED)
                if (status == TransferStatus.CANCELLED) {
                    if (!isSender) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Chỉ chi nhánh gửi hoặc cấp Quản lý mới có quyền hủy phiếu chuyển kho này!");
                        return "redirect:/admin/transfers/" + id;
                    }
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
