package com.bookstorechain.dto.request;

import com.bookstorechain.enums.BookCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBuybackDTO {

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    @NotNull(message = "Vui lòng chọn chi nhánh tiếp nhận")
    private Long storeId;

    @NotBlank(message = "Vui lòng nhập tựa sách")
    private String bookTitle;

    private String bookAuthor;

    @NotNull(message = "Vui lòng chọn tình trạng sách ước tính")
    private BookCondition condition;

    private String note;
}
