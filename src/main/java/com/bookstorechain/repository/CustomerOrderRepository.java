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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    boolean existsByOrderCode(String orderCode);

    Optional<CustomerOrder> findByOrderCode(String orderCode);

    Optional<CustomerOrder> findByOrderCodeAndCustomerId(String orderCode, Long customerId);

    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /** Đơn của một chi nhánh theo trạng thái, cũ nhất trước (danh sách đơn chờ shipper nhận). */
    List<CustomerOrder> findByStoreIdAndStatusOrderByIdAsc(Long storeId, OrderStatus status);

    /** Đơn một shipper đang giữ theo trạng thái, cũ nhất trước. */
    List<CustomerOrder> findByShipperIdAndStatusOrderByIdAsc(Long shipperId, OrderStatus status);

    /** 20 đơn gần nhất của shipper ở các trạng thái cho trước (lịch sử giao hàng). */
    List<CustomerOrder> findTop20ByShipperIdAndStatusInOrderByIdDesc(Long shipperId, java.util.Collection<OrderStatus> statuses);

    /** Khách đã có đơn (chưa huỷ) dùng mã này chưa; dùng cho mã giới hạn mỗi khách một lần. */
    boolean existsByCustomerIdAndPromotionIdAndStatusNot(Long customerId, Long promotionId, OrderStatus status);

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

    // ---- Thống kê doanh thu: chỉ tính đơn đã hoàn tất, theo ngày hoàn tất [from, to); storeId null là mọi chi nhánh ----

    /** Một dòng [doanh thu, số đơn, tổng giảm giá, tổng phí giao hàng]. */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0), COUNT(o), COALESCE(SUM(o.discountAmount), 0), COALESCE(SUM(o.shippingFee), 0) " +
           "FROM CustomerOrder o WHERE o.status = com.bookstorechain.enums.OrderStatus.COMPLETED AND o.completedAt >= :from AND o.completedAt < :to AND (:storeId IS NULL OR o.store.id = :storeId)")
    List<Object[]> completedTotals(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("storeId") Long storeId);

    /** Các dòng [ngày, doanh thu, số đơn] của những ngày có bán hàng. */
    @Query("SELECT CAST(o.completedAt AS LocalDate), SUM(o.totalAmount), COUNT(o) FROM CustomerOrder o WHERE o.status = com.bookstorechain.enums.OrderStatus.COMPLETED AND o.completedAt >= :from AND o.completedAt < :to AND (:storeId IS NULL OR o.store.id = :storeId)" +
           " GROUP BY CAST(o.completedAt AS LocalDate)")
    List<Object[]> dailyRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("storeId") Long storeId);

    /** Các dòng [tên chi nhánh, doanh thu], chi nhánh bán nhiều nhất trước. */
    @Query("SELECT o.store.name, SUM(o.totalAmount) FROM CustomerOrder o WHERE o.status = com.bookstorechain.enums.OrderStatus.COMPLETED AND o.completedAt >= :from AND o.completedAt < :to AND (:storeId IS NULL OR o.store.id = :storeId)" +
           " GROUP BY o.store.name ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> revenueByStore(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("storeId") Long storeId);

    /** Các dòng [hình thức thanh toán, doanh thu]. */
    @Query("SELECT o.payment.method, SUM(o.totalAmount) FROM CustomerOrder o WHERE o.status = com.bookstorechain.enums.OrderStatus.COMPLETED AND o.completedAt >= :from AND o.completedAt < :to AND (:storeId IS NULL OR o.store.id = :storeId)" +
           " GROUP BY o.payment.method")
    List<Object[]> revenueByPaymentMethod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("storeId") Long storeId);

    /** Các dòng [trạng thái, số đơn] của mọi đơn (không giới hạn thời gian). */
    @Query("SELECT o.status, COUNT(o) FROM CustomerOrder o WHERE (:storeId IS NULL OR o.store.id = :storeId) GROUP BY o.status")
    List<Object[]> countByStatus(@Param("storeId") Long storeId);
}
