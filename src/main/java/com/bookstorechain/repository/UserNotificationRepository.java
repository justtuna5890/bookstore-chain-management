package com.bookstorechain.repository;

import com.bookstorechain.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    Page<UserNotification> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    /** Đánh dấu đã đọc một thông báo của đúng người đó; trả về 0 nếu không có hoặc không phải của họ. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserNotification n SET n.read = true WHERE n.id = :id AND n.user.id = :userId AND n.read = false")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserNotification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllRead(@Param("userId") Long userId);

    /**
     * Không xoá persistence context: được gọi giữa một nghiệp vụ khác (mở cuộc chat) đang giữ các thực thể liên quan,
     * xoá context sẽ làm chúng bị tách và lần truy cập lazy sau đó báo LazyInitializationException.
     */
    @Modifying
    @Query("UPDATE UserNotification n SET n.read = true WHERE n.user.id = :userId AND n.link = :link AND n.read = false")
    int markReadByLink(@Param("userId") Long userId, @Param("link") String link);
}
