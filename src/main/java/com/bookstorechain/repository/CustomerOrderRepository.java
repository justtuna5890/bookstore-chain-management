package com.bookstorechain.repository;

import com.bookstorechain.entity.CustomerOrder;
import com.bookstorechain.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    boolean existsByOrderCode(String orderCode);

    Optional<CustomerOrder> findByOrderCode(String orderCode);

    Optional<CustomerOrder> findByOrderCodeAndCustomerId(String orderCode, Long customerId);

    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /** Khoá dòng đơn hàng khi đổi trạng thái để huỷ của khách và xác nhận của nhân viên không ghi đè nhau. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CustomerOrder o WHERE o.orderCode = :code AND o.customer.id = :customerId")
    Optional<CustomerOrder> lockByOrderCodeAndCustomerId(@Param("code") String code, @Param("customerId") Long customerId);

    /** Khoá dòng đơn hàng theo id cho nhân viên đổi trạng thái. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CustomerOrder o WHERE o.id = :id")
    Optional<CustomerOrder> lockById(@Param("id") Long id);

    @Query("SELECT o FROM CustomerOrder o WHERE " +
           "(:storeId IS NULL OR o.store.id = :storeId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR o.receiverPhone LIKE CONCAT('%', :keyword, '%'))")
    Page<CustomerOrder> search(@Param("storeId") Long storeId,
                               @Param("status") OrderStatus status,
                               @Param("keyword") String keyword,
                               Pageable pageable);
}
