package com.bookstorechain.repository;

import com.bookstorechain.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCode(String code);

    boolean existsByCode(String code);

    /** Các mã đang chạy và còn lượt (chưa lọc theo chi nhánh), mã sắp hết hạn trước. */
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.startsAt <= :now AND p.endsAt >= :now " +
           "AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit) ORDER BY p.endsAt ASC")
    List<Promotion> findRunning(@Param("now") LocalDateTime now);

    /**
     * Trừ lượt dùng nguyên tử: chỉ thành công khi cộng thêm n lượt vẫn không vượt giới hạn.
     * Trả về 0 nếu mã vừa hết lượt do khách khác dùng trước.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount + :n " +
           "WHERE p.id = :id AND (p.usageLimit IS NULL OR p.usedCount + :n <= p.usageLimit)")
    int consume(@Param("id") Long id, @Param("n") int n);

    /** Trả lại một lượt khi đơn dùng mã bị huỷ; không bao giờ xuống dưới 0. */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount - 1 WHERE p.id = :id AND p.usedCount > 0")
    int release(@Param("id") Long id);
}
