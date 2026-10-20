package com.bookstorechain.repository;

import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.enums.CopyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    boolean existsByCopyCode(String copyCode);

    java.util.Optional<BookCopy> findByCopyCode(String copyCode);

    @Query("SELECT bc FROM BookCopy bc WHERE " +
           "(:storeId IS NULL OR bc.store.id = :storeId) AND " +
           "(:status IS NULL OR bc.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(bc.copyCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(bc.book.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BookCopy> searchBookCopies(@Param("storeId") Long storeId,
                                    @Param("status") CopyStatus status,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    @Query("SELECT bc.store.name, COUNT(bc) FROM BookCopy bc GROUP BY bc.store.name")
    java.util.List<Object[]> countCopiesByStore();

    @Query("SELECT bc.store.name, SUM(bc.price) FROM BookCopy bc GROUP BY bc.store.name")
    java.util.List<Object[]> sumPriceByStore();

    @Query("SELECT bc.bookCondition, COUNT(bc) FROM BookCopy bc GROUP BY bc.bookCondition")
    java.util.List<Object[]> countByCondition();

    @Query("SELECT bc.bookCondition, COUNT(bc) FROM BookCopy bc WHERE bc.store.id = :storeId GROUP BY bc.bookCondition")
    java.util.List<Object[]> countByConditionAndStore(@Param("storeId") Long storeId);

    long countByStoreId(Long storeId);

    @Query("SELECT COALESCE(SUM(bc.price), 0) FROM BookCopy bc WHERE bc.store.id = :storeId")
    java.math.BigDecimal sumPriceByStoreId(@Param("storeId") Long storeId);

    java.util.List<BookCopy> findByBookIdAndStatus(Long bookId, CopyStatus status);

    java.util.List<BookCopy> findByStatus(CopyStatus status);
}