package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** Các sách trong giỏ thuộc cùng một chi nhánh, mỗi nhóm sẽ trở thành một đơn hàng khi thanh toán. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartStoreGroupDTO {
    private Long storeId;
    private String storeName;
    private List<CartItemDTO> items;
    /** Chỉ cộng các bản sách còn khả dụng. */
    private BigDecimal subtotal;
}
