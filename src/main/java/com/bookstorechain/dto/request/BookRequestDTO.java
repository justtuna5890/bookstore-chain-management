package com.bookstorechain.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BookRequestDTO {

    @NotBlank(message = "Tên sách không được để trống")
    @Size(max = 200, message = "Tên sách tối đa 200 ký tự")
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    @Size(max = 100, message = "Tên tác giả tối đa 100 ký tự")
    private String author;

    @Size(max = 100, message = "Nhà xuất bản tối đa 100 ký tự")
    private String publisher;

    @Min(value = 1000, message = "Năm xuất bản không hợp lệ")
    @Max(value = 2100, message = "Năm xuất bản không hợp lệ")
    private Integer publicationYear;

    @Size(max = 50, message = "Thể loại tối đa 50 ký tự")
    private String category;

    private String description;

    @PositiveOrZero(message = "Giá bìa không được âm")
    private BigDecimal coverPrice;

    private String coverImage;

    // Bổ sung thêm trường này để hứng file từ form
    private org.springframework.web.multipart.MultipartFile imageFile;
}