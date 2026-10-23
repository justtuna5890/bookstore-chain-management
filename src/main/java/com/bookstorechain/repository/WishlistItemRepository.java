package com.bookstorechain.repository;

import com.bookstorechain.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
