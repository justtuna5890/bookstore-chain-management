package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.ReviewRequestDTO;
import com.bookstorechain.dto.response.OrderLineDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.dto.response.ReviewDTO;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.OrderService;
import com.bookstorechain.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerOrderControllerTest {

    private static final long USER_ID = 7L;
    private static final String CODE = "OB-20261027-ABCDEF";

    @Mock private OrderService orderService;
    @Mock private ReviewService reviewService;

    private CustomerOrderController controller;
    private CustomUserDetails principal;
    private RedirectAttributes redirect;

    @BeforeEach
    void setUp() {
        controller = new CustomerOrderController(orderService, reviewService);
        User user = new User();
        user.setId(USER_ID);
        principal = new CustomUserDetails(user);
        redirect = new RedirectAttributesModelMap();
    }

    private OrderSummaryDTO order(OrderStatus status, long... bookIds) {
        List<OrderLineDTO> lines = new java.util.ArrayList<>();
        long copyId = 1;
        for (long bookId : bookIds) {
            lines.add(OrderLineDTO.builder().copyId(copyId++).bookId(bookId).title("Sach " + bookId).build());
        }
        return OrderSummaryDTO.builder().orderCode(CODE).status(status).lines(lines).build();
    }

    private ReviewRequestDTO form(Integer rating) {
        return ReviewRequestDTO.builder().rating(rating).comment("Sach dep").build();
    }

    private BindingResult noErrors(ReviewRequestDTO form) {
        return new BeanPropertyBindingResult(form, "form");
    }

    @Test
    @DisplayName("Đơn đã hoàn tất: trang chi tiết có khung đánh giá, mỗi đầu sách một khung dù đơn có hai bản")
    void detailOfCompletedOrderListsEachBookOnce() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenReturn(order(OrderStatus.COMPLETED, 100, 100, 200));
        ReviewDTO mine = ReviewDTO.builder().bookId(100L).rating(4).build();
        when(reviewService.getMyReview(USER_ID, 100L)).thenReturn(Optional.of(mine));
        when(reviewService.getMyReview(USER_ID, 200L)).thenReturn(Optional.empty());
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.detail(CODE, principal, model);

        assertThat(view).isEqualTo("customer/order-detail");
        @SuppressWarnings("unchecked")
        java.util.Collection<OrderLineDTO> lines = (java.util.Collection<OrderLineDTO>) model.get("reviewLines");
        assertThat(lines).extracting(OrderLineDTO::getBookId).containsExactly(100L, 200L);
        @SuppressWarnings("unchecked")
        Map<Long, ReviewDTO> mineByBook = (Map<Long, ReviewDTO>) model.get("myReviews");
        assertThat(mineByBook).containsOnlyKeys(100L).containsEntry(100L, mine);
    }

    @Test
    @DisplayName("Đơn chưa hoàn tất: trang chi tiết không có khung đánh giá")
    void detailOfOpenOrderHasNoReviewSection() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenReturn(order(OrderStatus.CONFIRMED, 100));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.detail(CODE, principal, model);

        assertThat(model.containsAttribute("reviewLines")).isFalse();
        verify(reviewService, never()).getMyReview(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Đơn không tồn tại hoặc của người khác: 404")
    void detailOfUnknownOrderIs404() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenThrow(new ResourceNotFoundException("khong co"));

        assertThatThrownBy(() -> controller.detail(CODE, principal, new ExtendedModelMap()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("Gửi đánh giá cho sách trong đơn đã hoàn tất: lưu đánh giá và về lại mục đánh giá của đơn")
    void reviewBookInCompletedOrder() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenReturn(order(OrderStatus.COMPLETED, 100));
        ReviewRequestDTO form = form(5);

        String view = controller.review(CODE, 100L, form, noErrors(form), principal, redirect);

        verify(reviewService).submit(USER_ID, 100L, form);
        assertThat(view).isEqualTo("redirect:/orders/" + CODE + "#danh-gia");
        assertThat(redirect.getFlashAttributes()).containsKey("reviewSuccess").doesNotContainKey("reviewError");
    }

    @Test
    @DisplayName("Đơn chưa hoàn tất: không cho đánh giá")
    void rejectReviewOnOpenOrder() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenReturn(order(OrderStatus.SHIPPING, 100));
        ReviewRequestDTO form = form(5);

        controller.review(CODE, 100L, form, noErrors(form), principal, redirect);

        verify(reviewService, never()).submit(anyLong(), anyLong(), any());
        assertThat(redirect.getFlashAttributes()).containsKey("reviewError");
    }

    @Test
    @DisplayName("Sách không có trong đơn: không cho đánh giá bằng cách đổi bookId trong URL")
    void rejectReviewForBookNotInOrder() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenReturn(order(OrderStatus.COMPLETED, 100));
        ReviewRequestDTO form = form(5);

        controller.review(CODE, 999L, form, noErrors(form), principal, redirect);

        verify(reviewService, never()).submit(anyLong(), anyLong(), any());
        assertThat(redirect.getFlashAttributes()).containsKey("reviewError");
    }

    @Test
    @DisplayName("Số sao không hợp lệ: báo lỗi, không lưu")
    void rejectInvalidRating() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenReturn(order(OrderStatus.COMPLETED, 100));
        ReviewRequestDTO form = form(null);
        BindingResult errors = noErrors(form);
        errors.rejectValue("rating", "NotNull", "Vui lòng chọn số sao");

        controller.review(CODE, 100L, form, errors, principal, redirect);

        verify(reviewService, never()).submit(anyLong(), anyLong(), any());
        assertThat(redirect.getFlashAttributes()).containsKey("reviewError");
    }

    @Test
    @DisplayName("Lỗi nghiệp vụ từ dịch vụ đánh giá được hiển thị cho khách")
    void businessErrorIsShown() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenReturn(order(OrderStatus.COMPLETED, 100));
        ReviewRequestDTO form = form(3);
        when(reviewService.submit(USER_ID, 100L, form)).thenThrow(new BusinessException("Bạn chỉ đánh giá được những cuốn sách đã mua và nhận hàng."));

        controller.review(CODE, 100L, form, noErrors(form), principal, redirect);

        assertThat(redirect.getFlashAttributes().get("reviewError")).isEqualTo("Bạn chỉ đánh giá được những cuốn sách đã mua và nhận hàng.");
    }

    @Test
    @DisplayName("Đánh giá trên đơn của người khác: 404, không lưu gì")
    void reviewOnSomeoneElsesOrderIs404() {
        when(orderService.getCustomerOrder(USER_ID, CODE)).thenThrow(new ResourceNotFoundException("khong co"));
        ReviewRequestDTO form = form(5);

        assertThatThrownBy(() -> controller.review(CODE, 100L, form, noErrors(form), principal, redirect))
                .isInstanceOf(ResponseStatusException.class);
        verify(reviewService, never()).submit(anyLong(), anyLong(), any());
    }
}
