package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.CheckoutRequestDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.CartService;
import com.bookstorechain.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử tranh chấp lượt dùng mã giảm giá trên SQL Server thật: hai khách cùng đặt hàng với một mã sắp hết lượt.
 * Chỉ chạy khi RUN_DB_TESTS=true vì ghi vào database dùng chung (tạo một mã tạm, tự dọn sau mỗi vòng và khi kết thúc).
 *
 * Chạy: mvn test -Dtest=PromotionConcurrencyIT
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class PromotionConcurrencyIT {

    private static final String CODE = "ITRACE";

    @Autowired private OrderService orderService;
    @Autowired private CartService cartService;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbc;

    private CheckoutRequestDTO checkoutWithCode() {
        return CheckoutRequestDTO.builder().receiverName("Khach Test").receiverPhone("0900000000")
                .fulfillmentType(FulfillmentType.PICKUP).paymentMethod(PaymentMethod.COD).promotionCode(CODE).build();
    }

    private void createPromotion(int usageLimit) {
        jdbc.update("DELETE FROM promotion WHERE code = ?", CODE);
        jdbc.update("INSERT INTO promotion (created_at, updated_at, code, description, type, value, min_order_amount, "
                + "starts_at, ends_at, usage_limit, used_count, one_per_customer, store_id, is_active) "
                + "VALUES (SYSDATETIME(), SYSDATETIME(), ?, N'Ma kiem thu tranh chap', N'FIXED', 10000, 0, "
                + "DATEADD(day, -1, SYSDATETIME()), DATEADD(day, 1, SYSDATETIME()), ?, 0, 1, NULL, 1)", CODE, usageLimit);
    }

    private long promotionId() {
        return jdbc.queryForObject("SELECT id FROM promotion WHERE code = ?", Long.class, CODE);
    }

    private List<Long> availableCopies(long storeId, int count) {
        List<Long> ids = jdbc.queryForList("SELECT TOP " + count + " id FROM book_copy WHERE status = 'AVAILABLE' AND store_id = ? ORDER BY id",
                Long.class, storeId);
        assertThat(ids).as("cần %d bản AVAILABLE ở chi nhánh %d", count, storeId).hasSize(count);
        return ids;
    }

    private Callable<List<OrderSummaryDTO>> checkoutAfter(CyclicBarrier barrier, User user) {
        return () -> {
            barrier.await(10, TimeUnit.SECONDS);
            return orderService.checkout(user.getId(), checkoutWithCode());
        };
    }

    private void record(Map<String, Integer> outcomes, Future<List<OrderSummaryDTO>> future) throws Exception {
        try {
            future.get(30, TimeUnit.SECONDS);
            outcomes.merge("OK", 1, Integer::sum);
        } catch (ExecutionException e) {
            outcomes.merge(e.getCause() instanceof BusinessException ? "BusinessException" : e.getCause().getClass().getSimpleName(), 1, Integer::sum);
        }
    }

    private void cleanUp(long lastOrderIdBefore, List<Long> copyIds, User a, User b) {
        jdbc.update("DELETE FROM payment WHERE order_id > ?", lastOrderIdBefore);
        jdbc.update("DELETE FROM order_item WHERE order_id > ?", lastOrderIdBefore);
        jdbc.update("DELETE FROM customer_order WHERE id > ?", lastOrderIdBefore);
        for (long id : copyIds) {
            jdbc.update("UPDATE book_copy SET status = 'AVAILABLE' WHERE id = ?", id);
        }
        jdbc.update("UPDATE promotion SET used_count = 0 WHERE code = ?", CODE);
        cartService.clear(a.getId());
        cartService.clear(b.getId());
    }

    @Test
    @DisplayName("Mã còn đúng 1 lượt, hai khách đặt cùng lúc qua 20 vòng: chỉ một khách được giảm, người kia rollback sạch")
    void lastUseGoesToExactlyOneCustomer() throws Exception {
        User a = userRepository.findByUsername("customer").orElseThrow();
        User b = userRepository.findByUsername("khachhang").orElseThrow();
        List<Long> hanoi = availableCopies(1, 2);
        createPromotion(1);
        long promotionId = promotionId();

        Map<String, Integer> outcomes = new TreeMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < 20; round++) {
                cartService.clear(a.getId());
                cartService.clear(b.getId());
                cartService.addItem(a.getId(), hanoi.get(0));
                cartService.addItem(b.getId(), hanoi.get(1));
                long lastOrderIdBefore = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM customer_order", Long.class);

                CyclicBarrier together = new CyclicBarrier(2);
                Future<List<OrderSummaryDTO>> fa = pool.submit(checkoutAfter(together, a));
                Future<List<OrderSummaryDTO>> fb = pool.submit(checkoutAfter(together, b));
                record(outcomes, fa);
                record(outcomes, fb);

                int usedCount = jdbc.queryForObject("SELECT used_count FROM promotion WHERE id = ?", Integer.class, promotionId);
                int ordersWithCode = jdbc.queryForObject("SELECT COUNT(*) FROM customer_order WHERE id > ? AND promotion_id = ?",
                        Integer.class, lastOrderIdBefore, promotionId);
                int ordersTotal = jdbc.queryForObject("SELECT COUNT(*) FROM customer_order WHERE id > ?", Integer.class, lastOrderIdBefore);
                int reserved = jdbc.queryForObject("SELECT COUNT(*) FROM book_copy WHERE id IN (?, ?) AND status = 'RESERVED'",
                        Integer.class, hanoi.get(0), hanoi.get(1));

                assertThat(usedCount).as("vòng %d: số lượt đã dùng", round).isEqualTo(1);
                assertThat(ordersWithCode).as("vòng %d: số đơn mang mã", round).isEqualTo(1);
                // Khách thua rollback hoàn toàn: không có đơn thứ hai, bản sách của họ không bị giữ
                assertThat(ordersTotal).as("vòng %d: tổng số đơn", round).isEqualTo(1);
                assertThat(reserved).as("vòng %d: số bản sách bị giữ", round).isEqualTo(1);

                cleanUp(lastOrderIdBefore, hanoi, a, b);
            }
        } finally {
            pool.shutdownNow();
            cartService.clear(a.getId());
            cartService.clear(b.getId());
            jdbc.update("DELETE FROM payment WHERE order_id IN (SELECT id FROM customer_order WHERE promotion_id = ?)", promotionId);
            jdbc.update("DELETE FROM order_item WHERE order_id IN (SELECT id FROM customer_order WHERE promotion_id = ?)", promotionId);
            jdbc.update("DELETE FROM customer_order WHERE promotion_id = ?", promotionId);
            jdbc.update("DELETE FROM promotion WHERE code = ?", CODE);
        }

        System.out.println("Kết quả 20 vòng (2 lần đặt mỗi vòng): " + outcomes);
        assertThat(outcomes.getOrDefault("OK", 0)).isEqualTo(20);
        assertThat(outcomes.getOrDefault("BusinessException", 0)).isEqualTo(20);
        assertThat(outcomes.keySet()).allMatch(key -> key.equals("OK") || key.equals("BusinessException"));
    }

    @Test
    @DisplayName("Mã còn 3 lượt, mỗi khách cần 2 lượt (giỏ 2 chi nhánh): không bao giờ dùng quá giới hạn")
    void multiOrderCheckoutsNeverExceedTheLimit() throws Exception {
        User a = userRepository.findByUsername("customer").orElseThrow();
        User b = userRepository.findByUsername("khachhang").orElseThrow();
        List<Long> hanoi = availableCopies(1, 2);
        List<Long> saigon = availableCopies(2, 2);
        List<Long> allCopies = List.of(hanoi.get(0), hanoi.get(1), saigon.get(0), saigon.get(1));
        createPromotion(3);
        long promotionId = promotionId();

        Map<String, Integer> outcomes = new TreeMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < 15; round++) {
                cartService.clear(a.getId());
                cartService.clear(b.getId());
                cartService.addItem(a.getId(), hanoi.get(0));
                cartService.addItem(a.getId(), saigon.get(0));
                cartService.addItem(b.getId(), hanoi.get(1));
                cartService.addItem(b.getId(), saigon.get(1));
                long lastOrderIdBefore = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM customer_order", Long.class);

                CyclicBarrier together = new CyclicBarrier(2);
                Future<List<OrderSummaryDTO>> fa = pool.submit(checkoutAfter(together, a));
                Future<List<OrderSummaryDTO>> fb = pool.submit(checkoutAfter(together, b));
                record(outcomes, fa);
                record(outcomes, fb);

                int usedCount = jdbc.queryForObject("SELECT used_count FROM promotion WHERE id = ?", Integer.class, promotionId);
                int ordersWithCode = jdbc.queryForObject("SELECT COUNT(*) FROM customer_order WHERE id > ? AND promotion_id = ?",
                        Integer.class, lastOrderIdBefore, promotionId);

                assertThat(usedCount).as("vòng %d: không vượt giới hạn 3", round).isLessThanOrEqualTo(3);
                assertThat(usedCount).as("vòng %d: số lượt đã dùng khớp số đơn mang mã", round).isEqualTo(ordersWithCode);
                // Mỗi giỏ tạo 2 đơn đều mang mã, nên chỉ một giỏ (2 lượt) lọt vào được
                assertThat(ordersWithCode).as("vòng %d", round).isEqualTo(2);

                cleanUp(lastOrderIdBefore, allCopies, a, b);
            }
        } finally {
            pool.shutdownNow();
            cartService.clear(a.getId());
            cartService.clear(b.getId());
            jdbc.update("DELETE FROM payment WHERE order_id IN (SELECT id FROM customer_order WHERE promotion_id = ?)", promotionId);
            jdbc.update("DELETE FROM order_item WHERE order_id IN (SELECT id FROM customer_order WHERE promotion_id = ?)", promotionId);
            jdbc.update("DELETE FROM customer_order WHERE promotion_id = ?", promotionId);
            jdbc.update("DELETE FROM promotion WHERE code = ?", CODE);
        }

        System.out.println("Kết quả 15 vòng giỏ 2 chi nhánh: " + outcomes);
        assertThat(outcomes.getOrDefault("OK", 0)).isEqualTo(15);
        assertThat(outcomes.keySet()).allMatch(key -> key.equals("OK") || key.equals("BusinessException"));
    }
}
