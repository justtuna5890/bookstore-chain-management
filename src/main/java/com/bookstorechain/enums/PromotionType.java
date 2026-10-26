package com.bookstorechain.enums;

public enum PromotionType {
    PERCENT("Giảm theo phần trăm"), // Giảm value% tổng tạm tính, có thể giới hạn mức giảm tối đa
    FIXED("Giảm số tiền cố định");  // Giảm đúng value đồng, không vượt quá tổng tạm tính

    private final String label;

    PromotionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
