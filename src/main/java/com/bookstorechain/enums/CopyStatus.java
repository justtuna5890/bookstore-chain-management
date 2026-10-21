package com.bookstorechain.enums;

public enum CopyStatus {
    AVAILABLE,  // Đang bày bán tại cửa hàng
    SOLD,       // Đã bán cho khách
    IN_TRANSIT, // Đang chuyển giữa các chi nhánh
    HOLDING     // Đang giữ đơn hàng / Chờ thanh toán
}