package com.bookstorechain.scheduler;

import com.bookstorechain.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Quét định kỳ để báo khách khi sách trong danh sách yêu thích có hàng lại. Dùng quét thay vì gắn vào từng chỗ nhập kho
 * vì sách có thể có hàng từ nhiều nguồn (thêm bản sách, thu mua, chuyển kho, huỷ đơn) và quét bắt được tất cả.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WishlistRestockScheduler {

    private final WishlistService wishlistService;

    @Scheduled(initialDelayString = "${app.wishlist.restock-check-ms:30000}", fixedDelayString = "${app.wishlist.restock-check-ms:30000}")
    public void notifyBackInStock() {
        try {
            int sent = wishlistService.notifyBackInStock();
            if (sent > 0) {
                log.info("Đã báo {} sách yêu thích có hàng lại", sent);
            }
        } catch (Exception e) {
            // Lỗi tạm thời (mất kết nối DB...) không được làm dừng tác vụ định kỳ; chu kỳ sau sẽ thử lại
            log.error("Lỗi khi quét sách yêu thích có hàng lại", e);
        }
    }
}
