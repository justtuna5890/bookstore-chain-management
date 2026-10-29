package com.bookstorechain.enums;

public enum CopyStatus {
    AVAILABLE,  // Đang bày bán tại cửa hàng
    SOLD,       // Đã bán cho khách
    IN_TRANSIT, // Đang chuyển giữa các chi nhánh
    HOLDING,    // Nhân viên tạm giữ thủ công / chờ phiếu chuyển kho
    RESERVED    // Đang được giữ cho đơn hàng của khách (chờ xác nhận hoặc giao nhận)
}
