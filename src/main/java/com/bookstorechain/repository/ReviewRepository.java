package com.bookstorechain.repository;

import com.bookstorechain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);

    /** Đánh giá đang hiển thị của một đầu sách; thứ tự theo Pageable. */
    List<Review> findByBookIdAndHiddenFalse(Long bookId, Pageable pageable);

    /** Một dòng [điểm trung bình, số đánh giá] của các đánh giá đang hiển thị; điểm trung bình null nếu chưa có. */
    @Query("SELECT AVG(r.rating), COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.hidden = false")
    List<Object[]> stats(@Param("bookId") Long bookId);

    /** Các dòng [số sao, số đánh giá] của các đánh giá đang hiển thị. */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.hidden = false GROUP BY r.rating")
    List<Object[]> distribution(@Param("bookId") Long bookId);

    /** Các dòng [id sách, điểm trung bình, số đánh giá] cho nhiều đầu sách trong một truy vấn. */
    @Query("SELECT r.book.id, AVG(r.rating), COUNT(r) FROM Review r WHERE r.book.id IN :ids AND r.hidden = false GROUP BY r.book.id")
    List<Object[]> summaries(@Param("ids") Collection<Long> ids);

    /**
     * Tìm đánh giá cho trang kiểm duyệt (kể cả đánh giá đã ẩn). hidden và rating null là không lọc;
     * pattern dạng %từ khoá% khớp tên sách, tên hoặc tên đăng nhập của người đánh giá, hoặc nội dung nhận xét.
     */
    @Query(value = "SELECT r FROM Review r JOIN FETCH r.book b JOIN FETCH r.user u WHERE "
            + "(:hidden IS NULL OR r.hidden = :hidden) AND (:rating IS NULL OR r.rating = :rating) AND "
            + "(LOWER(b.title) LIKE :pattern ESCAPE '\\' OR LOWER(u.fullName) LIKE :pattern ESCAPE '\\' OR LOWER(u.username) LIKE :pattern ESCAPE '\\' OR LOWER(r.comment) LIKE :pattern ESCAPE '\\')",
            countQuery = "SELECT COUNT(r) FROM Review r JOIN r.book b JOIN r.user u WHERE "
                    + "(:hidden IS NULL OR r.hidden = :hidden) AND (:rating IS NULL OR r.rating = :rating) AND "
                    + "(LOWER(b.title) LIKE :pattern ESCAPE '\\' OR LOWER(u.fullName) LIKE :pattern ESCAPE '\\' OR LOWER(u.username) LIKE :pattern ESCAPE '\\' OR LOWER(r.comment) LIKE :pattern ESCAPE '\\')")
    Page<Review> search(@Param("pattern") String pattern, @Param("hidden") Boolean hidden, @Param("rating") Integer rating, Pageable pageable);

    long countByHidden(boolean hidden);
}
