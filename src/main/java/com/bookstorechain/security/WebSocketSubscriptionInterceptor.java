package com.bookstorechain.security;

import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiểm soát ai được đăng ký (SUBSCRIBE) kênh nào trên broker STOMP. Mặc định từ chối, chỉ cho phép:
 * <ul>
 *   <li>/topic/notifications: nhân viên, quản lý, quản trị (thông báo thu mua, chuyển kho);</li>
 *   <li>/topic/store/{id}: quản trị mọi chi nhánh; quản lý và nhân viên chỉ chi nhánh của mình;</li>
 *   <li>/topic/store/all: quản trị và quản lý (đơn hàng của mọi chi nhánh);</li>
 *   <li>/user/queue/notifications: mọi người dùng đã đăng nhập, chỉ nhận thông báo gửi riêng cho chính mình
 *       (Spring ghép đích này với tên đăng nhập của phiên, nên không nghe được kênh của người khác).</li>
 * </ul>
 * Người chưa đăng nhập không đăng ký được kênh nào, khách hàng chỉ đăng ký được kênh riêng của mình. Khung bị từ chối
 * bị bỏ im lặng thay vì ném lỗi để trình duyệt không rơi vào vòng lặp kết nối lại. Kênh riêng của chat hỗ trợ sẽ
 * được bổ sung cùng tính năng đó.
 */
@Component
@Slf4j
public class WebSocketSubscriptionInterceptor implements ChannelInterceptor {

    static final String GENERAL_TOPIC = "/topic/notifications";
    static final String ALL_STORES_TOPIC = "/topic/store/all";
    static final String PERSONAL_NOTIFICATIONS = "/user/queue/notifications";
    private static final Pattern STORE_TOPIC = Pattern.compile("^/topic/store/(\\d+)$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }
        if (isAllowed(accessor.getUser(), accessor.getDestination())) {
            return message;
        }
        log.warn("Từ chối đăng ký kênh WebSocket {} của {}", accessor.getDestination(),
                accessor.getUser() == null ? "người chưa đăng nhập" : accessor.getUser().getName());
        return null;
    }

    boolean isAllowed(Principal principal, String destination) {
        CustomUserDetails signedIn = signedInUser(principal);
        if (signedIn == null || destination == null) {
            return false;
        }
        if (PERSONAL_NOTIFICATIONS.equals(destination)) {
            return true;
        }
        CustomUserDetails user = staffOf(signedIn);
        if (user == null) {
            return false;
        }
        Role role = user.getUser().getRole();

        if (GENERAL_TOPIC.equals(destination)) {
            return true;
        }
        if (ALL_STORES_TOPIC.equals(destination)) {
            return role == Role.ADMIN || role == Role.MANAGER;
        }
        Matcher store = STORE_TOPIC.matcher(destination);
        if (store.matches()) {
            if (role == Role.ADMIN) {
                return true;
            }
            Store own = user.getUser().getStore();
            return own != null && own.getId() != null && String.valueOf(own.getId()).equals(store.group(1));
        }
        return false;
    }

    /** Trả về người dùng của phiên WebSocket nếu đã đăng nhập (mọi vai trò); ngược lại null. */
    private CustomUserDetails signedInUser(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof CustomUserDetails details) {
            return details;
        }
        return null;
    }

    /** Trả về người dùng nếu là nhân viên, quản lý hoặc quản trị; ngược lại null. */
    private CustomUserDetails staffOf(CustomUserDetails details) {
        Role role = details.getUser().getRole();
        return role == Role.ADMIN || role == Role.MANAGER || role == Role.STAFF ? details : null;
    }
}
