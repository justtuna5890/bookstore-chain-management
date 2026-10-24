package com.bookstorechain.enums;

public enum OrderStatus {
    PENDING("Chờ xác nhận"),                // Khách vừa đặt, chờ nhân viên xác nhận
    CONFIRMED("Đã xác nhận"),               // Nhân viên đã xác nhận, đang chuẩn bị hàng
    READY_FOR_PICKUP("Sẵn sàng nhận hàng"), // Sẵn sàng để khách đến nhận tại chi nhánh
    SHIPPING("Đang giao"),                  // Đang giao tận nơi
    COMPLETED("Hoàn tất"),                  // Hoàn tất, bản sách chuyển sang SOLD
    CANCELLED("Đã huỷ");                    // Đã huỷ, bản sách trả về AVAILABLE

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
