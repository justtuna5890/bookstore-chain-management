package com.bookstorechain.repository;

import com.bookstorechain.entity.ChatConversation;
import com.bookstorechain.enums.ChatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    /**
     * Khoá dòng để các tin nhắn gửi đồng thời cộng số chưa đọc và cập nhật tin mới nhất không đè nhau.
     * Quan trọng: chỉ gọi khi cuộc trò chuyện CHƯA được nạp trong transaction hiện tại. Nếu đã nạp trước đó, Hibernate trả lại
     * bản cũ trong bộ nhớ thay vì đọc lại dưới khoá và lần lưu sau sẽ ghi đè cập nhật của người khác; vì vậy việc tra cứu
     * trước khi khoá phải dùng findIdByCustomerAndStore / findStoreIdOwnedBy (trả về giá trị đơn, không nạp thực thể).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChatConversation c WHERE c.id = :id")
    Optional<ChatConversation> lockById(@Param("id") Long id);

    @Query("SELECT c.id FROM ChatConversation c WHERE c.customer.id = :customerId AND c.store.id = :storeId")
    Optional<Long> findIdByCustomerAndStore(@Param("customerId") Long customerId, @Param("storeId") Long storeId);

    /** Chi nhánh của cuộc trò chuyện nếu nó thuộc về khách này; rỗng nếu không có hoặc là của người khác. */
    @Query("SELECT c.store.id FROM ChatConversation c WHERE c.id = :id AND c.customer.id = :customerId")
    Optional<Long> findStoreIdOwnedBy(@Param("id") Long id, @Param("customerId") Long customerId);

    @Query("SELECT c FROM ChatConversation c JOIN FETCH c.store JOIN FETCH c.customer WHERE c.customer.id = :customerId "
            + "ORDER BY c.lastMessageAt DESC, c.id DESC")
    List<ChatConversation> findByCustomer(@Param("customerId") Long customerId);

    /** Hộp thư của nhân viên: storeId null là mọi chi nhánh, status null là mọi trạng thái; cuộc có tin mới nhất lên đầu. */
    @Query("SELECT c FROM ChatConversation c JOIN FETCH c.store JOIN FETCH c.customer WHERE "
            + "(:storeId IS NULL OR c.store.id = :storeId) AND (:status IS NULL OR c.status = :status) "
            + "ORDER BY c.lastMessageAt DESC, c.id DESC")
    List<ChatConversation> findForStaff(@Param("storeId") Long storeId, @Param("status") ChatStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM ChatConversation c WHERE c.staffUnread > 0 AND (:storeId IS NULL OR c.store.id = :storeId)")
    long countWithStaffUnread(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(c) FROM ChatConversation c WHERE c.customer.id = :customerId AND c.customerUnread > 0")
    long countWithCustomerUnread(@Param("customerId") Long customerId);
}
