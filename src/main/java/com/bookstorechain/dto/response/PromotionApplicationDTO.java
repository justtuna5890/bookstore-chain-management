package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/** Kết quả áp một mã giảm giá lên giỏ hàng: tổng giảm và phần giảm của từng chi nhánh có đơn đủ điều kiện. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionApplicationDTO {
    private Long promotionId;
    private String code;
    private String description;
    private BigDecimal totalDiscount;
    /** Khoá là id chi nhánh; chi nhánh không đủ điều kiện không có trong bản đồ. */
    private Map<Long, BigDecimal> discountByStoreId;
}
