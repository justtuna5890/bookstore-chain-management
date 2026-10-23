package com.bookstorechain.controller.web;

import com.bookstorechain.dto.request.ReviewRequestDTO;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.security.CustomUserDetails;
import com.bookstorechain.service.CustomerBookService;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BookDetailController {

    private static final int REVIEW_LIMIT = 50;

    private final CustomerBookService customerBookService;
    private final ReviewService reviewService;

    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        try {
            model.addAttribute("book", customerBookService.getBookDetail(id));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        model.addAttribute("ratingSummary", reviewService.getSummary(id));
        model.addAttribute("reviews", reviewService.getReviews(id, REVIEW_LIMIT));
        if (userDetails != null) {
            Long userId = userDetails.getUser().getId();
            model.addAttribute("canReview", reviewService.canReview(userId, id));
            reviewService.getMyReview(userId, id).ifPresent(mine -> model.addAttribute("myReview", mine));
        }
        return "customer/book-detail";
    }

    @PostMapping("/books/{id}/reviews")
    public String submitReview(@PathVariable Long id,
                               @Valid @ModelAttribute ReviewRequestDTO form,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reviewError", "Vui lòng chọn từ 1 đến 5 sao; nhận xét tối đa 1000 ký tự.");
            return redirectToReviews(id);
        }
        try {
            reviewService.submit(userDetails.getUser().getId(), id, form);
            redirectAttributes.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã đánh giá cuốn sách này!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        } catch (PessimisticLockingFailureException e) {
            redirectAttributes.addFlashAttribute("reviewError", "Hệ thống đang bận, vui lòng thử lại sau vài giây.");
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        return redirectToReviews(id);
    }

    @PostMapping("/books/{id}/reviews/delete")
    public String deleteReview(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        reviewService.deleteOwn(userDetails.getUser().getId(), id);
        redirectAttributes.addFlashAttribute("reviewSuccess", "Đã xoá đánh giá của bạn.");
        return redirectToReviews(id);
    }

    private String redirectToReviews(Long bookId) {
        return "redirect:/books/" + bookId + "#reviews";
    }
}
