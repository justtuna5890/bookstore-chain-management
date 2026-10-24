package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.PromotionDTO;
import com.bookstorechain.entity.Promotion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Chuyển Promotion sang DTO kèm trạng thái tại thời điểm now. Gọi trong transaction vì đọc chi nhánh lazy. */
@Component
public class PromotionMapper {

    public PromotionDTO toDTO(Promotion promotion, LocalDateTime now) {
        return PromotionDTO.builder()
                .id(promotion.getId())
                .code(promotion.getCode())
                .description(promotion.getDescription())
                .type(promotion.getType())
                .value(promotion.getValue())
                .minOrderAmount(promotion.getMinOrderAmount())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .startsAt(promotion.getStartsAt())
                .endsAt(promotion.getEndsAt())
                .usageLimit(promotion.getUsageLimit())
                .usedCount(promotion.getUsedCount())
                .onePerCustomer(promotion.isOnePerCustomer())
                .storeId(promotion.getStore() == null ? null : promotion.getStore().getId())
                .storeName(promotion.getStore() == null ? null : promotion.getStore().getName())
                .active(promotion.isActive())
                .status(statusOf(promotion, now))
                .build();
    }

    static String statusOf(Promotion promotion, LocalDateTime now) {
        if (!promotion.isActive()) {
            return "PAUSED";
        }
        if (now.isBefore(promotion.getStartsAt())) {
            return "UPCOMING";
        }
        if (now.isAfter(promotion.getEndsAt())) {
            return "EXPIRED";
        }
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            return "EXHAUSTED";
        }
        return "RUNNING";
    }
}
