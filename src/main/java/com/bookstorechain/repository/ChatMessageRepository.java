package com.bookstorechain.repository;

import com.bookstorechain.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Các tin mới nhất của cuộc trò chuyện, mới nhất trước (giới hạn bằng Pageable); nơi gọi đảo lại để hiển thị cũ trước. */
    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.conversation c JOIN FETCH c.store JOIN FETCH c.customer "
            + "WHERE c.id = :conversationId ORDER BY m.createdAt DESC, m.id DESC")
    List<ChatMessage> findLatest(@Param("conversationId") Long conversationId, Pageable pageable);

    /** Số tin người dùng gửi từ một thời điểm, để chặn gửi dồn dập. */
    long countBySenderIdAndCreatedAtAfter(Long senderId, LocalDateTime since);
}
