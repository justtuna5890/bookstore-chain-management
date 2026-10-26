package com.bookstorechain.service;

import com.bookstorechain.dto.response.UserNotificationDTO;
import com.bookstorechain.enums.UserNotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Thông báo riêng cho từng người dùng: lưu lại và đẩy realtime qua kênh /user/queue/notifications. */
public interface UserNotificationService {

    /**
     * Lưu thông báo và đẩy cho người nhận nếu họ đang trực tuyến. Lưu cùng transaction của nơi gọi (đổi trạng thái
     * đơn và thông báo cùng thành công hoặc cùng huỷ); khung WebSocket chỉ được gửi sau khi transaction commit.
     * Lỗi đẩy realtime không bao giờ làm hỏng nghiệp vụ gọi vào.
     */
    void notify(Long userId, UserNotificationType type, String title, String message, String link);

    /** Các thông báo mới nhất, mới nhất trước. */
    List<UserNotificationDTO> getRecent(Long userId, int limit);

    Page<UserNotificationDTO> getPage(Long userId, Pageable pageable);

    long countUnread(Long userId);

    /** Đánh dấu đã đọc thông báo của chính người dùng; trả về false nếu không có hoặc không phải của họ. */
    boolean markRead(Long userId, Long notificationId);

    int markAllRead(Long userId);

    /** Đánh dấu đã đọc mọi thông báo chưa đọc của người dùng trỏ tới một liên kết (VD khi khách mở cuộc chat). */
    int markReadByLink(Long userId, String link);
}
