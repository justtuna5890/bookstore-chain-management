package com.bookstorechain.dto.response;

import com.bookstorechain.enums.BookCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Long copyId;
    private String copyCode;
    private Long bookId;
    private String title;
    private String author;
    private String coverImage;
    private BookCondition bookCondition;
    private BigDecimal price;
    private Long storeId;
    private String storeName;
    /** false khi bản sách đã bị khách khác đặt, đã bán hoặc đang chuyển kho. */
    private boolean available;
}
