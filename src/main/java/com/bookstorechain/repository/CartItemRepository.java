package com.bookstorechain.repository;

import com.bookstorechain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartIdOrderByCreatedAtAsc(Long cartId);

    Optional<CartItem> findByCartIdAndCopyId(Long cartId, Long copyId);

    boolean existsByCartIdAndCopyId(Long cartId, Long copyId);

    long countByCartId(Long cartId);

    long countByCartUserId(Long userId);
}
