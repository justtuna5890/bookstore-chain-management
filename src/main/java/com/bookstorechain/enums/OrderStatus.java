package com.bookstorechain.enums;

public enum OrderStatus {
    PENDING,          // Khách vừa đặt, chờ nhân viên xác nhận
    CONFIRMED,        // Nhân viên đã xác nhận, đang chuẩn bị hàng
    READY_FOR_PICKUP, // Sẵn sàng để khách đến nhận tại chi nhánh
    SHIPPING,         // Đang giao tận nơi
    COMPLETED,        // Hoàn tất, bản sách chuyển sang SOLD
    CANCELLED         // Đã huỷ, bản sách trả về AVAILABLE
}
