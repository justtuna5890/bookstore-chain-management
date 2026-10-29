package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {
    @Builder.Default
    private List<CartStoreGroupDTO> groups = new ArrayList<>();
    private int itemCount;
    private int availableCount;
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    public boolean isEmptyCart() {
        return itemCount == 0;
    }

    public boolean isHasUnavailable() {
        return availableCount < itemCount;
    }

    /** Số đơn hàng sẽ được tạo: mỗi chi nhánh còn sách khả dụng là một đơn. */
    public long getOrderCount() {
        return groups.stream().filter(g -> g.getItems().stream().anyMatch(CartItemDTO::isAvailable)).count();
    }
}
