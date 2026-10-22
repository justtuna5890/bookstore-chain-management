package com.bookstorechain.repository;

import com.bookstorechain.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByOrderCode(String orderCode);

    Optional<CustomerOrder> findByOrderCodeAndCustomerId(String orderCode, Long customerId);

    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    long countByCreatedAtBetween(java.time.LocalDateTime from, java.time.LocalDateTime to);
}
