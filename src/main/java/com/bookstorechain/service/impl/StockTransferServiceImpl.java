package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.StockTransferRequestDTO;
import com.bookstorechain.dto.response.StockTransferResponseDTO;
import com.bookstorechain.entity.*;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.TransferStatus;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.*;
import com.bookstorechain.service.NotificationService;
import com.bookstorechain.service.StockTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final BookCopyRepository bookCopyRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<StockTransferResponseDTO> getTransfers(Long storeId, TransferStatus status, Pageable pageable) {
        return stockTransferRepository.searchTransfers(storeId, status, pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransfer getTransferById(Long id) {
        return stockTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu chuyển kho với ID: " + id));
    }

    @Override
    @Transactional
    public void createTransfer(StockTransferRequestDTO requestDTO, String currentUsername) {
        if (requestDTO.getSenderStoreId().equals(requestDTO.getReceiverStoreId())) {
            throw new IllegalArgumentException("Chi nhánh gửi và chi nhánh nhận không được trùng nhau!");
        }

        Store senderStore = storeRepository.findById(requestDTO.getSenderStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh gửi"));

        Store receiverStore = storeRepository.findById(requestDTO.getReceiverStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh nhận"));

        User creator = userRepository.findByUsername(currentUsername)
                .or(() -> userRepository.findByEmail(currentUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản thao tác"));

        // Nếu người thao tác là STAFF thì chỉ được tạo phiếu xuất từ đúng chi nhánh của mình
        if (creator.getRole() == com.bookstorechain.enums.Role.STAFF) {
            if (creator.getStore() == null || !creator.getStore().getId().equals(senderStore.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Nhân viên chỉ có quyền tạo phiếu điều chuyển từ chi nhánh trực thuộc của mình!");
            }
        }

        StockTransfer transfer = StockTransfer.builder()
                .senderStore(senderStore)
                .receiverStore(receiverStore)
                .creator(creator)
                .status(TransferStatus.PENDING)
                .note(requestDTO.getNote())
                .build();

        for (Long copyId : requestDTO.getBookCopyIds()) {
            BookCopy bookCopy = bookCopyRepository.findById(copyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản sách ID: " + copyId));

            // Kiểm tra sách phải thuộc chi nhánh gửi
            if (!bookCopy.getStore().getId().equals(senderStore.getId())) {
                throw new IllegalArgumentException("Bản sách " + bookCopy.getCopyCode() + " không thuộc kho chi nhánh gửi!");
            }

            // Kiểm tra trạng thái sách phải là AVAILABLE
            if (bookCopy.getStatus() != CopyStatus.AVAILABLE) {
                throw new IllegalStateException("Bản sách " + bookCopy.getCopyCode() + " hiện không ở trạng thái sẵn sàng (AVAILABLE) để điều chuyển! Trạng thái hiện tại: " + bookCopy.getStatus());
            }

            // Kiểm tra sách không nằm trong phiếu điều chuyển khác đang PENDING hoặc IN_TRANSIT
            if (stockTransferRepository.isBookCopyInActiveTransfer(copyId, List.of(TransferStatus.PENDING, TransferStatus.IN_TRANSIT))) {
                throw new IllegalStateException("Bản sách " + bookCopy.getCopyCode() + " đang nằm trong một phiếu điều chuyển khác chưa hoàn tất!");
            }

            // Khóa sách sang trạng thái HOLDING ngay khi tạo phiếu PENDING để ngăn bán tại quầy
            bookCopy.setStatus(CopyStatus.HOLDING);
            bookCopyRepository.save(bookCopy);

            StockTransferItem item = StockTransferItem.builder()
                    .stockTransfer(transfer)
                    .bookCopy(bookCopy)
                    .build();
            transfer.getItems().add(item);
        }

        StockTransfer saved = stockTransferRepository.save(transfer);

        // Bắn thông báo thời gian thực WebSocket tới chi nhánh nhận và ban quản lý
        notificationService.notifyNewStockTransfer(saved);
    }

    @Override
    @Transactional
    public void updateTransferStatus(Long id, TransferStatus newStatus) {
        StockTransfer transfer = getTransferById(id);
        TransferStatus currentStatus = transfer.getStatus();

        // Xử lý chuyển đổi trạng thái và cập nhật BookCopy tương ứng
        if (currentStatus == TransferStatus.COMPLETED || currentStatus == TransferStatus.CANCELLED) {
            throw new IllegalStateException("Không thể thay đổi trạng thái của phiếu đã hoàn tất hoặc đã hủy!");
        }

        // Ràng buộc quy trình trạng thái:
        // PENDING -> chỉ được sang IN_TRANSIT hoặc CANCELLED
        if (currentStatus == TransferStatus.PENDING && newStatus == TransferStatus.COMPLETED) {
            throw new IllegalStateException("Phiếu chuyển kho phải qua trạng thái Đang vận chuyển (IN_TRANSIT) trước khi Hoàn tất (COMPLETED)!");
        }

        if (newStatus == TransferStatus.IN_TRANSIT) {
            // Chuyển sang đang vận chuyển: Đổi trạng thái các BookCopy thành IN_TRANSIT
            for (StockTransferItem item : transfer.getItems()) {
                BookCopy bc = item.getBookCopy();
                bc.setStatus(CopyStatus.IN_TRANSIT);
                bookCopyRepository.save(bc);
            }
        } else if (newStatus == TransferStatus.COMPLETED) {
            // Hoàn tất: Cập nhật chi nhánh chủ sở hữu mới cho các BookCopy và trả về AVAILABLE
            for (StockTransferItem item : transfer.getItems()) {
                BookCopy bc = item.getBookCopy();
                bc.setStore(transfer.getReceiverStore());
                bc.setStatus(CopyStatus.AVAILABLE);
                bookCopyRepository.save(bc);
            }
        } else if (newStatus == TransferStatus.CANCELLED) {
            // Hủy phiếu: Trả lại trạng thái AVAILABLE cho sách ở chi nhánh cũ (giải phóng từ HOLDING hoặc IN_TRANSIT)
            for (StockTransferItem item : transfer.getItems()) {
                BookCopy bc = item.getBookCopy();
                bc.setStatus(CopyStatus.AVAILABLE);
                bookCopyRepository.save(bc);
            }
        }

        transfer.setStatus(newStatus);
        stockTransferRepository.save(transfer);
    }

    private StockTransferResponseDTO mapToResponseDTO(StockTransfer st) {
        return StockTransferResponseDTO.builder()
                .id(st.getId())
                .senderStoreName(st.getSenderStore().getName())
                .receiverStoreName(st.getReceiverStore().getName())
                .creatorUsername(st.getCreator().getUsername())
                .status(st.getStatus())
                .note(st.getNote())
                .itemCount(st.getItems() != null ? st.getItems().size() : 0)
                .createdAt(st.getCreatedAt())
                .build();
    }
}
