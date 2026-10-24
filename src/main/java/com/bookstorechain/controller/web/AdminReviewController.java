package com.bookstorechain.controller.web;

import com.bookstorechain.dto.response.ReviewDTO;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminReviewController {

    private static final int PAGE_SIZE = 10;

    private final AdminReviewService adminReviewService;

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "status", defaultValue = "all") String status,
                       @RequestParam(value = "rating", required = false) Integer rating,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        Integer ratingFilter = rating != null && rating >= 1 && rating <= 5 ? rating : null;
        model.addAttribute("reviewPage", adminReviewService.search(keyword, hiddenFilter(status), ratingFilter,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")))));
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("status", status);
        model.addAttribute("rating", ratingFilter);
        model.addAttribute("totalCount", adminReviewService.countAll());
        model.addAttribute("hiddenCount", adminReviewService.countHidden());
        return "admin/review/review-list";
    }

    @PostMapping("/{id}/hide")
    public String hide(@PathVariable Long id,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "rating", required = false) Integer rating,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       RedirectAttributes redirectAttributes) {
        return change(id, true, keyword, status, rating, page, redirectAttributes);
    }

    @PostMapping("/{id}/show")
    public String show(@PathVariable Long id,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "rating", required = false) Integer rating,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       RedirectAttributes redirectAttributes) {
        return change(id, false, keyword, status, rating, page, redirectAttributes);
    }

    private String change(Long id, boolean hidden, String keyword, String status, Integer rating, int page,
                          RedirectAttributes redirectAttributes) {
        try {
            ReviewDTO review = adminReviewService.setHidden(id, hidden);
            redirectAttributes.addFlashAttribute("successMessage", hidden
                    ? "Đã ẩn đánh giá của " + review.getReviewerName() + " về \"" + review.getBookTitle() + "\"."
                    : "Đã hiện lại đánh giá của " + review.getReviewerName() + " về \"" + review.getBookTitle() + "\".");
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        // Giữ nguyên bộ lọc và trang đang xem; chỉ ghép tham số đã mã hoá vào đường dẫn nội bộ cố định
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/admin/reviews");
        if (keyword != null && !keyword.isBlank()) {
            uri.queryParam("keyword", keyword.trim());
        }
        if (status != null && !status.isBlank() && !"all".equals(status)) {
            uri.queryParam("status", status);
        }
        if (rating != null) {
            uri.queryParam("rating", rating);
        }
        if (page > 0) {
            uri.queryParam("page", page);
        }
        return "redirect:" + uri.build().encode().toUriString();
    }

    /** "hidden" chỉ đánh giá đang ẩn, "visible" chỉ đánh giá đang hiển thị, còn lại là tất cả. */
    private Boolean hiddenFilter(String status) {
        if ("hidden".equals(status)) {
            return Boolean.TRUE;
        }
        return "visible".equals(status) ? Boolean.FALSE : null;
    }
}
