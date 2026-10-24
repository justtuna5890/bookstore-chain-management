package com.bookstorechain.service.impl;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.BuybackStatus;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.NotificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.bookstorechain.service.UserNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Chỉ kiểm tra phần báo cho khách khi yêu cầu thu mua đổi trạng thái; phần nhập kho thuộc luồng thu mua có sẵn. */
@ExtendWith(MockitoExtension.class)
class BuybackServiceImplTest {

    private static final long REQUEST_ID = 12L;

    @Mock private BuybackRequestRepository buybackRequestRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserNotificationService userNotificationService;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationService notificationService;

    private BuybackServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BuybackServiceImpl(buybackRequestRepository, bookCopyRepository, bookRepository, userNotificationService,
                storeRepository, userRepository, passwordEncoder, notificationService);
    }

    private BuybackRequest request(BuybackStatus status) {
        Store store = TestData.store(1L, "OldBook Ha Noi");
        BuybackRequest request = BuybackRequest.builder().customer(TestData.customer(7L)).store(store).status(status).build();
        request.setId(REQUEST_ID);
        when(buybackRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        return request;
    }

    @Test
    @DisplayName("Duyệt yêu cầu: báo cho khách mang sách đến đúng chi nhánh, kèm ghi chú của nhân viên")
    void approvedNotifiesCustomer() {
        request(BuybackStatus.PENDING);

        service.updateStatusAndPricing(REQUEST_ID, BuybackStatus.APPROVED, " Sách còn đẹp ", null, null);

        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.BUYBACK),
                contains("#BB-12 đã được duyệt"), contains("OldBook Ha Noi"), eq(null));
        verify(userNotificationService).notify(any(), any(), any(), contains("Ghi chú: Sách còn đẹp"), any());
    }

    @Test
    @DisplayName("Từ chối yêu cầu: báo cho khách kèm lý do")
    void rejectedNotifiesCustomerWithReason() {
        request(BuybackStatus.PENDING);

        service.updateStatusAndPricing(REQUEST_ID, BuybackStatus.REJECTED, "Sách quá cũ", null, null);

        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.BUYBACK),
                contains("bị từ chối"), contains("Sách quá cũ"), eq(null));
    }

    @Test
    @DisplayName("Hoàn tất nhập kho: báo cho khách")
    void completedNotifiesCustomer() {
        request(BuybackStatus.APPROVED);

        service.updateStatusAndPricing(REQUEST_ID, BuybackStatus.COMPLETED, null, null, null);

        verify(userNotificationService).notify(eq(7L), eq(UserNotificationType.BUYBACK),
                contains("đã hoàn tất"), contains("OldBook Ha Noi"), eq(null));
    }

    @Test
    @DisplayName("Lưu lại mà trạng thái không đổi thì không báo gì")
    void sameStatusDoesNotNotify() {
        request(BuybackStatus.PENDING);

        service.updateStatusAndPricing(REQUEST_ID, BuybackStatus.PENDING, "đang xem xét", null, null);

        verifyNoInteractions(userNotificationService);
    }

    @Test
    @DisplayName("Sửa giá hoặc ghi chú của yêu cầu đã duyệt (vẫn APPROVED) thì không báo lại cho khách")
    void resavingApprovedRequestDoesNotNotifyAgain() {
        request(BuybackStatus.APPROVED);

        service.updateStatusAndPricing(REQUEST_ID, BuybackStatus.APPROVED, "đã chỉnh giá", null, null);

        verifyNoInteractions(userNotificationService);
    }

    @Test
    @DisplayName("Yêu cầu đã đóng không đổi được nên cũng không báo")
    void closedRequestDoesNotNotify() {
        request(BuybackStatus.COMPLETED);

        assertThatThrownBy(() -> service.updateStatusAndPricing(REQUEST_ID, BuybackStatus.REJECTED, null, null, null))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(userNotificationService);
    }
}
