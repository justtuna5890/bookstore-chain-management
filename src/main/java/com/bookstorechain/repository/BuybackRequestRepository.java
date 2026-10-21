package com.bookstorechain.repository;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.enums.BuybackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuybackRequestRepository extends JpaRepository<BuybackRequest, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"customer", "store", "items"})
    @Query("SELECT br FROM BuybackRequest br WHERE " +
           "(:storeId IS NULL OR br.store.id = :storeId) AND " +
           "(:status IS NULL OR br.status = :status)")
    Page<BuybackRequest> searchRequests(@Param("storeId") Long storeId,
                                        @Param("status") BuybackStatus status,
                                        Pageable pageable);

    @Query("SELECT br.status, COUNT(br) FROM BuybackRequest br GROUP BY br.status")
    List<Object[]> countByStatus();

    @Query("SELECT br.status, COUNT(br) FROM BuybackRequest br WHERE br.store.id = :storeId GROUP BY br.status")
    List<Object[]> countByStatusAndStore(@Param("storeId") Long storeId);

    long countByStoreId(Long storeId);

    @Query("SELECT DISTINCT br FROM BuybackRequest br LEFT JOIN FETCH br.items LEFT JOIN FETCH br.store WHERE br.customer.id = :customerId ORDER BY br.createdAt DESC")
    List<BuybackRequest> findByCustomerIdWithItems(@Param("customerId") Long customerId);

    @Query("SELECT DISTINCT br FROM BuybackRequest br LEFT JOIN FETCH br.store LEFT JOIN FETCH br.items " +
           "WHERE (:storeId IS NULL OR br.store.id = :storeId) " +
           "AND br.createdAt >= :startDateTime AND br.createdAt <= :endDateTime " +
           "ORDER BY br.createdAt DESC")
    List<BuybackRequest> findForReport(@Param("storeId") Long storeId,
                                       @Param("startDateTime") java.time.LocalDateTime startDateTime,
                                       @Param("endDateTime") java.time.LocalDateTime endDateTime);
}
