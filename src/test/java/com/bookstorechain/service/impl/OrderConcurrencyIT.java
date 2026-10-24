package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.CheckoutRequestDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.PaymentMethod;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.CartService;
import com.bookstorechain.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Comparator;
import java.util.HashMap;
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
 * Kiểm thử tranh chấp thật trên SQL Server: hai khách cùng đặt một bản sách ở cùng một thời điểm.
 * Chỉ chạy khi đặt RUN_DB_TESTS=true (và DB_USERNAME/DB_PASSWORD trỏ tới database BookStoreChain đã seed),
 * vì test ghi vào database dùng chung. Test tự dọn đơn, giỏ và trạng thái bản sách sau mỗi vòng.
 *
 * Chạy: mvn test -Dtest=OrderConcurrencyIT
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class OrderConcurrencyIT {

    private static final int ROUNDS = 30;

    @Autowired private OrderService orderService;
    @Autowired private CartService cartService;
    @Autowired private UserRepository userRepository;
    @Autowired private BookCopyRepository bookCopyRepository;
    @Autowired private JdbcTemplate jdbc;

    private CheckoutRequestDTO pickup() {
        return CheckoutRequestDTO.builder().receiverName("Khach Test").receiverPhone("0900000000")
                .fulfillmentType(FulfillmentType.PICKUP).paymentMethod(PaymentMethod.COD).build();
    }

    @Test
    @DisplayName("Hai khách cùng đặt một cuốn qua 30 vòng: mỗi cuốn chỉ thuộc tối đa một đơn, không deadlock")
    void sharedCopyIsNeverSoldTwice() throws Exception {
        User a = userRepository.findByUsername("customer").orElseThrow();
        User b = userRepository.findByUsername("khachhang").orElseThrow();
        List<BookCopy> free = bookCopyRepository.findByStatus(CopyStatus.AVAILABLE).stream()
                .sorted(Comparator.comparing(BookCopy::getId)).limit(3).toList();
        assertThat(free).as("cần ít nhất 3 bản sách AVAILABLE trong database").hasSize(3);
        long shared = free.get(0).getId();
        long onlyA = free.get(1).getId();
        long onlyB = free.get(2).getId();

        Map<String, Integer> outcomes = new TreeMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < ROUNDS; round++) {
                cartService.clear(a.getId());
                cartService.clear(b.getId());
                cartService.addItem(a.getId(), shared);
                cartService.addItem(a.getId(), onlyA);
                cartService.addItem(b.getId(), shared);
                cartService.addItem(b.getId(), onlyB);
                long lastOrderIdBefore = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM customer_order", Long.class);

                CyclicBarrier startTogether = new CyclicBarrier(2);
                Future<List<OrderSummaryDTO>> fa = pool.submit(checkoutAfter(startTogether, a));
                Future<List<OrderSummaryDTO>> fb = pool.submit(checkoutAfter(startTogether, b));
                record(outcomes, fa);
                record(outcomes, fb);

                Map<Long, Integer> soldCount = new HashMap<>();
                jdbc.query("SELECT book_copy_id, COUNT(*) FROM order_item WHERE order_id > ? GROUP BY book_copy_id",
                        rs -> { soldCount.put(rs.getLong(1), rs.getInt(2)); }, lastOrderIdBefore);

                assertThat(soldCount.getOrDefault(shared, 0)).as("vòng %d: cuốn tranh chấp", round).isLessThanOrEqualTo(1);
                for (Long copyId : soldCount.keySet()) {
                    String status = jdbc.queryForObject("SELECT status FROM book_copy WHERE id = ?", String.class, copyId);
                    assertThat(status).as("vòng %d: bản %d nằm trong đơn phải là RESERVED", round, copyId).isEqualTo("RESERVED");
                }

                cleanUp(a, b, lastOrderIdBefore, shared, onlyA, onlyB);
            }
        } finally {
            pool.shutdownNow();
            cartService.clear(a.getId());
            cartService.clear(b.getId());
        }

        System.out.println("Kết quả " + ROUNDS + " vòng (2 lần đặt mỗi vòng): " + outcomes);
        assertThat(outcomes.keySet()).as("chỉ chấp nhận thành công hoặc BusinessException")
                .allMatch(key -> key.equals("OK") || key.equals("BusinessException"));
    }

    private Callable<List<OrderSummaryDTO>> checkoutAfter(CyclicBarrier barrier, User user) {
        return () -> {
            barrier.await(10, TimeUnit.SECONDS);
            return orderService.checkout(user.getId(), pickup());
        };
    }

    private void record(Map<String, Integer> outcomes, Future<List<OrderSummaryDTO>> future) throws Exception {
        try {
            future.get(30, TimeUnit.SECONDS);
            outcomes.merge("OK", 1, Integer::sum);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            outcomes.merge(cause instanceof BusinessException ? "BusinessException" : cause.getClass().getSimpleName(), 1, Integer::sum);
        }
    }

    private void cleanUp(User a, User b, long lastOrderIdBefore, long... copyIds) {
        jdbc.update("DELETE FROM payment WHERE order_id > ?", lastOrderIdBefore);
        jdbc.update("DELETE FROM order_item WHERE order_id > ?", lastOrderIdBefore);
        jdbc.update("DELETE FROM customer_order WHERE id > ?", lastOrderIdBefore);
        for (long id : copyIds) {
            jdbc.update("UPDATE book_copy SET status = 'AVAILABLE' WHERE id = ?", id);
        }
        cartService.clear(a.getId());
        cartService.clear(b.getId());
    }
}
