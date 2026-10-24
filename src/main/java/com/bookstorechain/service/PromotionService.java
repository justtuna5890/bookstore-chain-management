package com.bookstorechain.service;

import com.bookstorechain.dto.response.PromotionApplicationDTO;
import com.bookstorechain.dto.response.PromotionDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Mã giảm giá phía khách hàng khi đặt hàng. */
public interface PromotionService {

    /**
     * Kiểm tra mã và tính giảm giá cho giỏ, không ghi gì. subtotalByStoreId là tổng tạm tính của đơn từng chi nhánh.
     * Ném BusinessException với lý do cụ thể nếu mã không dùng được.
     */
    PromotionApplicationDTO evaluate(String code, Long userId, Map<Long, BigDecimal> subtotalByStoreId);

    /** Như evaluate nhưng trừ lượt dùng nguyên tử; phải gọi trong transaction đặt hàng để rollback trả lại lượt. */
    PromotionApplicationDTO redeem(String code, Long userId, Map<Long, BigDecimal> subtotalByStoreId);

    /** Trả lại một lượt khi đơn dùng mã bị huỷ. */
    void releaseUsage(Long promotionId);

    /** Các mã đang chạy và còn lượt, áp dụng được cho ít nhất một trong các chi nhánh trong giỏ. */
    List<PromotionDTO> getAvailablePromotions(Set<Long> storeIds);
}
