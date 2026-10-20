package com.bookstorechain.repository;

import com.bookstorechain.entity.StockTransfer;
import com.bookstorechain.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

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
                                     @Param("statuses") java.util.Collection<TransferStatus> statuses);

    @Query("SELECT COUNT(st) FROM StockTransfer st WHERE st.senderStore.id = :storeId OR st.receiverStore.id = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(item) > 0 FROM StockTransferItem item WHERE " +
           "item.bookCopy.id = :bookCopyId AND item.stockTransfer.status IN (:statuses)")
    boolean isBookCopyInActiveTransfer(@Param("bookCopyId") Long bookCopyId,
                                      @Param("statuses") java.util.Collection<TransferStatus> statuses);
}