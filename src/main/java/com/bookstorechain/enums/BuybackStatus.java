package com.bookstorechain.enums;

public enum BuybackStatus {
    PENDING,    // Mới gửi, chờ nhân viên tiếp nhận
    APPROVED,   // Đã duyệt định giá, chờ khách mang sách đến / tiếp nhận
    REJECTED,   // Từ chối thu mua
    COMPLETED   // Đã hoàn tất nhập kho BookCopy
}