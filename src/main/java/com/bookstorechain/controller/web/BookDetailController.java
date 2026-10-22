package com.bookstorechain.controller.web;

import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.service.CustomerBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class BookDetailController {

    private final CustomerBookService customerBookService;

    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("book", customerBookService.getBookDetail(id));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        return "customer/book-detail";
    }
}
