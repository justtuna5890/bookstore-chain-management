package com.bookstorechain.repository;

import com.bookstorechain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE " +
           "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByTitleAndAuthor(String title, String author);

    java.util.List<Book> findByIsActiveTrue();

    java.util.List<Book> findTop4ByCategoryAndIdNotAndIsActiveTrue(String category, Long id);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL AND b.isActive = true")
    java.util.List<String> findDistinctCategories();
}