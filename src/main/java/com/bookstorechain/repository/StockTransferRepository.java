package com.bookstorechain.repository;

import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"senderStore", "receiverStore", "creator", "items"})
    @Query("SELECT st FROM StockTransfer st WHERE " +
           "(:storeId IS NULL OR st.senderStore.id = :storeId OR st.receiverStore.id = :storeId) AND " +
           "(:status IS NULL OR st.status = :status)")
    Page<StockTransfer> searchTransfers(@Param("storeId") Long storeId,
                                        @Param("status") TransferStatus status,
                                        Pageable pageable);

    @Query("SELECT COUNT(st) > 0 FROM StockTransfer st WHERE " +
           "(st.senderStore.id = :storeId OR st.receiverStore.id = :storeId) AND " +
           "st.status IN (:statuses)")
    boolean existsByStoreAndStatuses(@Param("storeId") Long storeId,
                                     @Param("statuses") Collection<TransferStatus> statuses);

    @Query("SELECT COUNT(st) FROM StockTransfer st WHERE st.senderStore.id = :storeId OR st.receiverStore.id = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(item) > 0 FROM StockTransferItem item WHERE " +
           "item.bookCopy.id = :bookCopyId AND item.stockTransfer.status IN (:statuses)")
    boolean isBookCopyInActiveTransfer(@Param("bookCopyId") Long bookCopyId,
                                       @Param("statuses") Collection<TransferStatus> statuses);

    @Query("SELECT DISTINCT st FROM StockTransfer st " +
           "LEFT JOIN FETCH st.senderStore " +
           "LEFT JOIN FETCH st.receiverStore " +
           "LEFT JOIN FETCH st.creator " +
           "LEFT JOIN FETCH st.items item " +
           "WHERE (:storeId IS NULL OR st.senderStore.id = :storeId OR st.receiverStore.id = :storeId) " +
           "AND st.createdAt >= :startDateTime AND st.createdAt <= :endDateTime " +
           "ORDER BY st.createdAt DESC")
    List<StockTransfer> findForReport(@Param("storeId") Long storeId,
                                       @Param("startDateTime") LocalDateTime startDateTime,
                                       @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT COUNT(st) FROM StockTransfer st WHERE (:storeId IS NULL OR st.senderStore.id = :storeId OR st.receiverStore.id = :storeId) AND st.status IN (:statuses)")
    long countByStatusesAndStoreId(@Param("statuses") Collection<TransferStatus> statuses, @Param("storeId") Long storeId);

    @Query("SELECT DISTINCT st FROM StockTransfer st LEFT JOIN FETCH st.senderStore LEFT JOIN FETCH st.receiverStore " +
           "WHERE (:storeId IS NULL OR st.senderStore.id = :storeId OR st.receiverStore.id = :storeId) " +
           "AND st.status = com.bookstorechain.enums.TransferStatus.IN_TRANSIT AND st.createdAt <= :threshold ORDER BY st.createdAt ASC")
    List<StockTransfer> findDelayedInTransitTransfers(@Param("storeId") Long storeId,
                                                      @Param("threshold") LocalDateTime threshold);

    @Query("SELECT st.senderStore.name, st.receiverStore.name, COUNT(st) FROM StockTransfer st " +
           "WHERE st.status = com.bookstorechain.enums.TransferStatus.COMPLETED GROUP BY st.senderStore.name, st.receiverStore.name")
    List<Object[]> getCompletedTransferFlow();
}
