package com.bookstorechain.dto.response;

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
public class OrderLineDTO {
    private Long copyId;
    private Long bookId;
    private String title;
    private String copyCode;
    private BigDecimal price;
}
