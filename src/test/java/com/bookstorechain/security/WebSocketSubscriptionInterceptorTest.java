package com.bookstorechain.security;

import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WebSocketSubscriptionInterceptorTest {

    private final WebSocketSubscriptionInterceptor interceptor = new WebSocketSubscriptionInterceptor();
    private final MessageChannel channel = mock(MessageChannel.class);

    private Principal principal(Role role, Long storeId) {
        Store store = null;
        if (storeId != null) {
            store = Store.builder().name("Chi nhanh " + storeId).build();
            store.setId(storeId);
        }
        User user = User.builder().username(role.name().toLowerCase()).role(role).store(store).build();
        CustomUserDetails details = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    private Message<?> frame(StompCommand command, String destination, Principal user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        accessor.setUser(user);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private boolean subscribeAllowed(Principal user, String destination) {
        Message<?> message = frame(StompCommand.SUBSCRIBE, destination, user);
        return interceptor.preSend(message, channel) != null;
    }

    @Test
    @DisplayName("Người chưa đăng nhập không đăng ký được kênh nào")
    void anonymousIsDenied() {
        assertThat(subscribeAllowed(null, "/topic/notifications")).isFalse();
        assertThat(subscribeAllowed(null, "/topic/store/1")).isFalse();
        assertThat(subscribeAllowed(null, "/topic/store/all")).isFalse();
    }

    @Test
    @DisplayName("Khách hàng không nghe được kênh nội bộ của nhân viên")
    void customerIsDeniedEverywhere() {
        Principal customer = principal(Role.CUSTOMER, null);

        assertThat(subscribeAllowed(customer, "/topic/notifications")).isFalse();
        assertThat(subscribeAllowed(customer, "/topic/store/1")).isFalse();
        assertThat(subscribeAllowed(customer, "/topic/store/all")).isFalse();
    }

    @Test
    @DisplayName("Nhân viên chỉ nghe kênh chung và kênh chi nhánh của mình")
    void staffIsLimitedToOwnStore() {
        Principal staff = principal(Role.STAFF, 1L);

        assertThat(subscribeAllowed(staff, "/topic/notifications")).isTrue();
        assertThat(subscribeAllowed(staff, "/topic/store/1")).isTrue();
        assertThat(subscribeAllowed(staff, "/topic/store/2")).isFalse();
        assertThat(subscribeAllowed(staff, "/topic/store/all")).isFalse();
    }

    @Test
    @DisplayName("Nhân viên chưa gán chi nhánh không nghe được kênh chi nhánh nào")
    void staffWithoutStoreGetsNoStoreTopic() {
        Principal staff = principal(Role.STAFF, null);

        assertThat(subscribeAllowed(staff, "/topic/store/1")).isFalse();
        assertThat(subscribeAllowed(staff, "/topic/notifications")).isTrue();
    }

    @Test
    @DisplayName("Quản lý nghe kênh chi nhánh của mình và kênh mọi chi nhánh")
    void managerHasOwnStoreAndAllStores() {
        Principal manager = principal(Role.MANAGER, 1L);

        assertThat(subscribeAllowed(manager, "/topic/store/1")).isTrue();
        assertThat(subscribeAllowed(manager, "/topic/store/2")).isFalse();
        assertThat(subscribeAllowed(manager, "/topic/store/all")).isTrue();
        assertThat(subscribeAllowed(manager, "/topic/notifications")).isTrue();
    }

    @Test
    @DisplayName("Quản trị viên nghe được mọi kênh được hỗ trợ")
    void adminHasEverySupportedTopic() {
        Principal admin = principal(Role.ADMIN, null);

        assertThat(subscribeAllowed(admin, "/topic/notifications")).isTrue();
        assertThat(subscribeAllowed(admin, "/topic/store/all")).isTrue();
        assertThat(subscribeAllowed(admin, "/topic/store/1")).isTrue();
        assertThat(subscribeAllowed(admin, "/topic/store/99")).isTrue();
    }

    @Test
    @DisplayName("Kênh lạ hoặc sai định dạng bị từ chối với mọi vai trò")
    void unknownDestinationsAreDenied() {
        Principal admin = principal(Role.ADMIN, null);

        assertThat(subscribeAllowed(admin, "/topic/secret")).isFalse();
        assertThat(subscribeAllowed(admin, "/queue/anything")).isFalse();
        assertThat(subscribeAllowed(admin, "/topic/store/abc")).isFalse();
        assertThat(subscribeAllowed(admin, "/topic/store/1/extra")).isFalse();
        assertThat(subscribeAllowed(admin, "/topic/store/")).isFalse();
        assertThat(subscribeAllowed(admin, null)).isFalse();
    }

    @Test
    @DisplayName("Chỉ khung SUBSCRIBE bị kiểm tra, các khung khác đi qua nguyên vẹn")
    void otherFramesPassThrough() {
        Message<?> connect = frame(StompCommand.CONNECT, null, null);
        Message<?> send = frame(StompCommand.SEND, "/app/hello", null);

        assertThat(interceptor.preSend(connect, channel)).isSameAs(connect);
        assertThat(interceptor.preSend(send, channel)).isSameAs(send);
    }
}
