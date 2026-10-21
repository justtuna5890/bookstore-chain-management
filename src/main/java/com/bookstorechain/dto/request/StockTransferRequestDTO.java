package com.bookstorechain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StockTransferRequestDTO {

    @NotNull(message = "Vui lòng chọn chi nhánh gửi")
    private Long senderStoreId;

    @NotNull(message = "Vui lòng chọn chi nhánh nhận")
    private Long receiverStoreId;

    private String note;

    @NotEmpty(message = "Phiếu chuyển kho phải chọn ít nhất một bản sách")
    private List<Long> bookCopyIds; // Danh sách ID của BookCopy cần chuyển
}