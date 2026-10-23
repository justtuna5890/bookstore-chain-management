package com.bookstorechain.repository;

import com.bookstorechain.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByUserIdAndBookId(Long userId, Long bookId);

    /** Danh sách yêu thích của khách, thêm gần đây nhất trước, nạp sẵn đầu sách. */
    @Query("SELECT i FROM WishlistItem i JOIN FETCH i.book WHERE i.user.id = :userId ORDER BY i.createdAt DESC, i.id DESC")
    List<WishlistItem> findByUserIdWithBook(@Param("userId") Long userId);

    @Query("SELECT i.book.id FROM WishlistItem i WHERE i.user.id = :userId")
    List<Long> findBookIdsByUserId(@Param("userId") Long userId);

    /** Mục yêu thích chưa được báo mà sách đang bán và có ít nhất một bản sẵn sàng. */
    @Query("SELECT i FROM WishlistItem i JOIN FETCH i.user JOIN FETCH i.book b WHERE i.inStockNotified = false AND b.isActive = true "
            + "AND EXISTS (SELECT c.id FROM BookCopy c WHERE c.book = b AND c.status = com.bookstorechain.enums.CopyStatus.AVAILABLE)")
    List<WishlistItem> findRestocked();

    /** Bỏ cờ "đã báo" của các sách đã hết hàng để lần có hàng sau được báo lại; trả về số mục được đặt lại. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE WishlistItem i SET i.inStockNotified = false WHERE i.inStockNotified = true "
            + "AND NOT EXISTS (SELECT c.id FROM BookCopy c WHERE c.book = i.book AND c.status = com.bookstorechain.enums.CopyStatus.AVAILABLE)")
    int resetOutOfStock();
}
