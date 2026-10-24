package com.bookstorechain.repository;

import com.bookstorechain.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** Khách đã nhận hàng (đơn COMPLETED) có chứa một bản sách thuộc đầu sách này chưa. */
    @Query("SELECT COUNT(i) > 0 FROM OrderItem i WHERE i.order.customer.id = :userId " +
           "AND i.order.status = com.bookstorechain.enums.OrderStatus.COMPLETED AND i.copy.book.id = :bookId")
    boolean hasCompletedPurchase(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /** Các dòng [id sách, tên sách, số cuốn đã bán, doanh thu] của đơn hoàn tất trong [from, to), bán chạy nhất trước. */
    @Query("SELECT b.id, b.title, COUNT(i), SUM(i.priceSnapshot) FROM OrderItem i JOIN i.order o JOIN i.copy c JOIN c.book b WHERE " +
           "o.status = com.bookstorechain.enums.OrderStatus.COMPLETED AND o.completedAt >= :from AND o.completedAt < :to " +
           "AND (:storeId IS NULL OR o.store.id = :storeId) GROUP BY b.id, b.title ORDER BY COUNT(i) DESC, SUM(i.priceSnapshot) DESC")
    List<Object[]> topSellingBooks(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                   @Param("storeId") Long storeId, Pageable pageable);

    /** Số cuốn đã bán trong các đơn hoàn tất trong [from, to). */
    @Query("SELECT COUNT(i) FROM OrderItem i JOIN i.order o WHERE " +
           "o.status = com.bookstorechain.enums.OrderStatus.COMPLETED AND o.completedAt >= :from AND o.completedAt < :to " +
           "AND (:storeId IS NULL OR o.store.id = :storeId)")
    long countSoldItems(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("storeId") Long storeId);
}
