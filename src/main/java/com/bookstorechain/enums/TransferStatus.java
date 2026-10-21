package com.bookstorechain.enums;

public enum TransferStatus {
    PENDING,    // Mới tạo phiếu, chờ duyệt xuất kho
    IN_TRANSIT, // Đã xuất kho, sách đang trên đường vận chuyển
    COMPLETED,  // Chi nhánh nhận đã tiếp nhận thành công
    CANCELLED   // Hủy phiếu chuyển kho
}