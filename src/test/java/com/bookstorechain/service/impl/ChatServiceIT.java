package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.ChatMessageDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.service.ChatService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import com.bookstorechain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử chat trên SQL Server thật: các lỗi chỉ lộ ra với database thật (tải lazy sau khi xoá persistence context,
 * ràng buộc duy nhất khi hai tin đầu tiên gửi cùng lúc, số chưa đọc bị ghi đè khi tin đến đồng thời).
 * Chỉ chạy khi đặt RUN_DB_TESTS=true; dùng chi nhánh 4 (không có chat mẫu) và tự dọn dữ liệu sau mỗi test.
 *
 * Chạy: mvn test -Dtest=ChatServiceIT
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class ChatServiceIT {

    private static final long STORE = 4L;

    @Autowired private ChatService chatService;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbc;

    private Long customerId;
    private Long staffId;

    @BeforeEach
    void setUp() {
        customerId = userRepository.findByUsername("customer").map(User::getId).orElseThrow();
        staffId = userRepository.findByUsername("staff").map(User::getId).orElseThrow();
        cleanUp();
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM user_notification WHERE link LIKE '/chat?conversation=%' AND link NOT IN ('/chat?conversation=1') AND user_id = ?", customerId);
        jdbc.update("DELETE FROM chat_message WHERE conversation_id IN (SELECT id FROM chat_conversation WHERE customer_id = ? AND store_id = ?)", customerId, STORE);
        jdbc.update("DELETE FROM chat_conversation WHERE customer_id = ? AND store_id = ?", customerId, STORE);
    }

    private long conversationId() {
        return jdbc.queryForObject("SELECT id FROM chat_conversation WHERE customer_id = ? AND store_id = ?", Long.class, customerId, STORE);
    }

    private <T> List<T> runTogether(List<Callable<T>> tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        try {
            CyclicBarrier start = new CyclicBarrier(tasks.size());
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(60, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("Khách mở cuộc trò chuyện sau khi nhân viên trả lời: đọc được tin (không lỗi tải lazy), số chưa đọc và thông báo trên chuông về đã đọc")
    void customerCanOpenThreadAfterStaffReply() {
        chatService.sendFromCustomer(customerId, STORE, "Chi nhanh Can Tho co sach nay khong?");
        long id = conversationId();
        chatService.sendFromStaff(staffId, id, null, "Da con a.");
        assertThat(jdbc.queryForObject("SELECT customer_unread FROM chat_conversation WHERE id = ?", Integer.class, id)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_notification WHERE user_id = ? AND link = ? AND is_read = 0",
                Integer.class, customerId, "/chat?conversation=" + id)).isEqualTo(1);

        List<ChatMessageDTO> thread = chatService.getCustomerMessages(customerId, id);

        assertThat(thread).extracting(ChatMessageDTO::getContent).containsExactly("Chi nhanh Can Tho co sach nay khong?", "Da con a.");
        assertThat(thread.get(1).getSenderName()).startsWith("Nhân viên");
        assertThat(jdbc.queryForObject("SELECT customer_unread FROM chat_conversation WHERE id = ?", Integer.class, id)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_notification WHERE user_id = ? AND link = ? AND is_read = 0",
                Integer.class, customerId, "/chat?conversation=" + id)).isZero();
        assertThat(chatService.getStaffMessages(id, null)).hasSize(2);
    }

    @Test
    @DisplayName("15 tin đầu tiên của cùng một khách gửi đồng thời: một cuộc trò chuyện duy nhất, đủ 15 tin, không vi phạm ràng buộc")
    void simultaneousFirstMessagesCreateOneConversation() throws Exception {
        List<Callable<ChatMessageDTO>> tasks = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            int n = i;
            tasks.add(() -> chatService.sendFromCustomer(customerId, STORE, "Tin so " + n));
        }

        List<ChatMessageDTO> results = runTogether(tasks);

        assertThat(results).hasSize(15);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM chat_conversation WHERE customer_id = ? AND store_id = ?", Integer.class, customerId, STORE)).isEqualTo(1);
        long id = conversationId();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM chat_message WHERE conversation_id = ?", Integer.class, id)).isEqualTo(15);
        assertThat(jdbc.queryForObject("SELECT staff_unread FROM chat_conversation WHERE id = ?", Integer.class, id)).isEqualTo(15);
    }

    @Test
    @DisplayName("8 tin của khách và 8 tin của nhân viên đến cùng lúc: số chưa đọc của khách không bị ghi đè, không mất tin")
    void counterSurvivesConcurrentTraffic() throws Exception {
        chatService.sendFromCustomer(customerId, STORE, "Mo dau");
        long id = conversationId();
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int n = i;
            tasks.add(() -> chatService.sendFromCustomer(customerId, STORE, "Khach " + n));
            tasks.add(() -> chatService.sendFromStaff(staffId, id, null, "Nhan vien " + n));
        }

        runTogether(tasks);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM chat_message WHERE conversation_id = ?", Integer.class, id)).isEqualTo(17);
        assertThat(jdbc.queryForObject("SELECT customer_unread FROM chat_conversation WHERE id = ?", Integer.class, id)).isEqualTo(8);
    }
}
