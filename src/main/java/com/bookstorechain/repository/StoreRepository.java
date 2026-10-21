package com.bookstorechain.repository;

import com.bookstorechain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByName(String name);

    boolean existsByName(String name);

    List<Store> findByIsActiveTrue();
}