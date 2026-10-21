package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.NotificationDTO;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyNewBuyback(BuybackRequest request) {
        try {
            String storeName = request.getStore() != null ? request.getStore().getName() : "Toàn hệ thống";
            Long storeId = request.getStore() != null ? request.getStore().getId() : null;
            String customerName = request.getCustomer() != null ? request.getCustomer().getFullName() : "Khách hàng";

            NotificationDTO dto = NotificationDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Đơn Thu Mua Sách Mới (#BB-" + request.getId() + ")")
                    .message(customerName + " vừa gửi yêu cầu thu mua tại " + storeName)
                    .type("BUYBACK")
                    .storeId(storeId)
                    .storeName(storeName)
                    .targetUrl("/admin/buybacks/" + request.getId())
                    .timestamp(LocalDateTime.now())
                    .build();

            // Gửi tới topic chung cho ADMIN và MANAGER
            messagingTemplate.convertAndSend("/topic/notifications", dto);

            // Gửi tới topic riêng của chi nhánh tiếp nhận nếu có
            if (storeId != null) {
                messagingTemplate.convertAndSend("/topic/store/" + storeId, dto);
            }

            log.info("Đã phát thông báo WebSocket cho đơn thu mua #BB-{}", request.getId());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo WebSocket đơn thu mua", e);
        }
    }

    @Override
    public void notifyNewStockTransfer(StockTransfer transfer) {
        try {
            String senderName = transfer.getSenderStore() != null ? transfer.getSenderStore().getName() : "Kho gửi";
            String receiverName = transfer.getReceiverStore() != null ? transfer.getReceiverStore().getName() : "Kho nhận";
            Long receiverStoreId = transfer.getReceiverStore() != null ? transfer.getReceiverStore().getId() : null;
            int bookCount = transfer.getItems() != null ? transfer.getItems().size() : 0;

            NotificationDTO dto = NotificationDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Phiếu Chuyển Kho Mới (#TR-" + transfer.getId() + ")")
                    .message(senderName + " vừa lập phiếu chuyển " + bookCount + " cuốn sách đến " + receiverName)
                    .type("TRANSFER")
                    .storeId(receiverStoreId)
                    .storeName(receiverName)
                    .targetUrl("/admin/transfers/" + transfer.getId())
                    .timestamp(LocalDateTime.now())
                    .build();

            // Gửi tới topic chung
            messagingTemplate.convertAndSend("/topic/notifications", dto);

            // Gửi tới topic của chi nhánh nhận hàng
            if (receiverStoreId != null) {
                messagingTemplate.convertAndSend("/topic/store/" + receiverStoreId, dto);
            }

            log.info("Đã phát thông báo WebSocket cho phiếu chuyển kho #TR-{}", transfer.getId());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo WebSocket phiếu chuyển kho", e);
        }
    }
}
