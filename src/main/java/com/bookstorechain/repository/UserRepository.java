package com.bookstorechain.repository;

import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"store"})
    Optional<User> findByUsername(String username);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"store"})
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role") Role role,
                           Pageable pageable);

    long countByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.bookstorechain.enums.Role.ADMIN AND u.isActive = true")
    long countActiveAdmins();

    long countByRoleAndCreatedAtBetween(Role role, LocalDateTime start, LocalDateTime end);
}
