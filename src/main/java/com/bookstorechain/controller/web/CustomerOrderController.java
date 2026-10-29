package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.ReviewRequestDTO;
import com.bookstorechain.dto.response.OrderLineDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.dto.response.ReviewDTO;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.OrderService;
import com.bookstorechain.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final OrderService orderService;
    private final ReviewService reviewService;

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("orders", orderService.getCustomerOrders(userDetails.getUser().getId()));
        return "customer/order-list";
    }

    @GetMapping("/{code}")
    public String detail(@PathVariable String code, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        OrderSummaryDTO order;
        try {
            order = orderService.getCustomerOrder(userId, code);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        model.addAttribute("order", order);
        if (order.getStatus() == OrderStatus.COMPLETED) {
            // Mỗi đầu sách một khung đánh giá, dù đơn có nhiều bản của cùng một đầu sách
            Map<Long, OrderLineDTO> reviewLines = new LinkedHashMap<>();
            order.getLines().forEach(line -> reviewLines.putIfAbsent(line.getBookId(), line));
            Map<Long, ReviewDTO> myReviews = new HashMap<>();
            reviewLines.keySet().forEach(bookId ->
                    reviewService.getMyReview(userId, bookId).ifPresent(review -> myReviews.put(bookId, review)));
            model.addAttribute("reviewLines", reviewLines.values());
            model.addAttribute("myReviews", myReviews);
        }
        return "customer/order-detail";
    }

    /** Đánh giá ngay trên trang đơn: chỉ cho cuốn sách có trong đơn đã hoàn tất của chính khách. */
    @PostMapping("/{code}/reviews/{bookId}")
    public String review(@PathVariable String code,
                         @PathVariable Long bookId,
                         @Valid @ModelAttribute ReviewRequestDTO form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUser().getId();
        OrderSummaryDTO order;
        try {
            order = orderService.getCustomerOrder(userId, code);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("reviewError", "Bạn chỉ đánh giá được sau khi đơn hàng đã hoàn tất.");
        } else if (order.getLines().stream().noneMatch(line -> bookId.equals(line.getBookId()))) {
            redirectAttributes.addFlashAttribute("reviewError", "Cuốn sách này không có trong đơn hàng.");
        } else if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reviewError", "Vui lòng chọn từ 1 đến 5 sao; nhận xét tối đa 1000 ký tự.");
        } else {
            try {
                reviewService.submit(userId, bookId, form);
                redirectAttributes.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã đánh giá cuốn sách này!");
            } catch (BusinessException e) {
                redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
            } catch (PessimisticLockingFailureException e) {
                redirectAttributes.addFlashAttribute("reviewError", "Hệ thống đang bận, vui lòng thử lại sau vài giây.");
            }
        }
        return "redirect:/orders/" + code + "#danh-gia";
    }

    @PostMapping("/{code}/cancel")
    public String cancel(@PathVariable String code,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelByCustomer(userDetails.getUser().getId(), code);
            redirectAttributes.addFlashAttribute("orderSuccess", "Đã huỷ đơn hàng " + code + ". Các cuốn sách được mở bán trở lại.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("orderError", e.getMessage());
        } catch (PessimisticLockingFailureException e) {
            redirectAttributes.addFlashAttribute("orderError", "Hệ thống đang bận, vui lòng thử lại sau vài giây.");
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        return "redirect:/orders/" + code;
    }
}
