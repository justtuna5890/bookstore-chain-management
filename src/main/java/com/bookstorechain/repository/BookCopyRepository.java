package com.bookstorechain.repository;

import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.enums.CopyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    boolean existsByCopyCode(String copyCode);

    java.util.Optional<BookCopy> findByCopyCode(String copyCode);

    /**
     * Giữ chỗ nguyên tử: chỉ chuyển các bản sao đang AVAILABLE sang RESERVED trong một câu lệnh UPDATE.
     * So sánh số dòng trả về với số id gửi lên để biết có bản nào đã bị người khác lấy trước.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE BookCopy bc SET bc.status = com.bookstorechain.enums.CopyStatus.RESERVED " +
           "WHERE bc.id IN :ids AND bc.status = com.bookstorechain.enums.CopyStatus.AVAILABLE")
    int reserveIfAvailable(@Param("ids") java.util.Collection<Long> ids);

    @Query("SELECT bc FROM BookCopy bc WHERE " +
           "(:storeId IS NULL OR bc.store.id = :storeId) AND " +
           "(:status IS NULL OR bc.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(bc.copyCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(bc.book.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BookCopy> searchBookCopies(@Param("storeId") Long storeId,
                                    @Param("status") CopyStatus status,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    @Query("SELECT bc.store.name, COUNT(bc) FROM BookCopy bc WHERE bc.status <> com.bookstorechain.enums.CopyStatus.SOLD GROUP BY bc.store.name")
    List<Object[]> countCopiesByStore();

    @Query("SELECT bc.store.name, SUM(bc.price) FROM BookCopy bc WHERE bc.status <> com.bookstorechain.enums.CopyStatus.SOLD GROUP BY bc.store.name")
    List<Object[]> sumPriceByStore();

    @Query("SELECT bc.bookCondition, COUNT(bc) FROM BookCopy bc WHERE bc.status <> com.bookstorechain.enums.CopyStatus.SOLD GROUP BY bc.bookCondition")
    List<Object[]> countByCondition();

    @Query("SELECT bc.bookCondition, COUNT(bc) FROM BookCopy bc WHERE bc.store.id = :storeId AND bc.status <> com.bookstorechain.enums.CopyStatus.SOLD GROUP BY bc.bookCondition")
    List<Object[]> countByConditionAndStore(@Param("storeId") Long storeId);

    /** Số bản sao còn trong kho của chi nhánh (không tính sách đã bán cho khách). */
    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.store.id = :storeId AND bc.status <> com.bookstorechain.enums.CopyStatus.SOLD")
    long countByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COALESCE(SUM(bc.price), 0) FROM BookCopy bc WHERE bc.store.id = :storeId AND bc.status <> com.bookstorechain.enums.CopyStatus.SOLD")
    java.math.BigDecimal sumPriceByStoreId(@Param("storeId") Long storeId);

    List<BookCopy> findByBookIdAndStatus(Long bookId, CopyStatus status);

    List<BookCopy> findByStatus(CopyStatus status);

    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE (:storeId IS NULL OR bc.store.id = :storeId) AND bc.status = :status")
    long countByStatusAndStoreId(@Param("status") CopyStatus status, @Param("storeId") Long storeId);

    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE (:storeId IS NULL OR bc.store.id = :storeId) AND bc.status = com.bookstorechain.enums.CopyStatus.AVAILABLE AND bc.createdAt <= :beforeDate")
    long countStaleCopies(@Param("storeId") Long storeId, @Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT bc.book.title, COUNT(bc) FROM BookCopy bc WHERE (:storeId IS NULL OR bc.store.id = :storeId) AND bc.status = com.bookstorechain.enums.CopyStatus.AVAILABLE GROUP BY bc.book.id, bc.book.title HAVING COUNT(bc) <= 2")
    List<Object[]> findLowStockBooks(@Param("storeId") Long storeId);

    @Query("SELECT bc.status, COUNT(bc) FROM BookCopy bc WHERE (:storeId IS NULL OR bc.store.id = :storeId) GROUP BY bc.status")
    List<Object[]> countByStatusAndStoreIdGrouped(@Param("storeId") Long storeId);

    @Query("SELECT bc.bookCondition, COUNT(bc) FROM BookCopy bc WHERE (:storeId IS NULL OR bc.store.id = :storeId) AND bc.status <> com.bookstorechain.enums.CopyStatus.SOLD GROUP BY bc.bookCondition")
    List<Object[]> countByConditionAndOptionalStore(@Param("storeId") Long storeId);
}
