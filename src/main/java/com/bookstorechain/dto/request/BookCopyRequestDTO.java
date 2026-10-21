package com.bookstorechain.dto.request;

import com.bookstorechain.enums.BookCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BookCopyRequestDTO {

    @NotBlank(message = "Mã bản sách không được để trống")
    private String copyCode;

    @NotNull(message = "Vui lòng chọn đầu sách")
    private Long bookId;

    @NotNull(message = "Vui lòng chọn chi nhánh")
    private Long storeId;

    @NotNull(message = "Vui lòng chọn tình trạng sách")
    private BookCondition bookCondition;

    @NotNull(message = "Giá bán không được để trống")
    @Positive(message = "Giá bán phải lớn hơn 0")
    private BigDecimal price;

    private String note;
}