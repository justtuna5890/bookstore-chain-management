package com.bookstorechain.repository;

import com.bookstorechain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** Khách đã nhận hàng (đơn COMPLETED) có chứa một bản sách thuộc đầu sách này chưa. */
    @Query("SELECT COUNT(i) > 0 FROM OrderItem i WHERE i.order.customer.id = :userId " +
           "AND i.order.status = com.bookstorechain.enums.OrderStatus.COMPLETED AND i.copy.book.id = :bookId")
    boolean hasCompletedPurchase(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
