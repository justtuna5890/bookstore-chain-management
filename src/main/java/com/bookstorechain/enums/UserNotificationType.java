package com.bookstorechain.enums;

/** Nhóm thông báo gửi cho từng người dùng; giao diện dùng nhóm để chọn biểu tượng. */
public enum UserNotificationType {
    ORDER,      // Đơn hàng đổi trạng thái hoặc thanh toán
    WISHLIST,   // Sách yêu thích có hàng lại
    BUYBACK,    // Yêu cầu thu mua sách cũ được xử lý
    REVIEW      // Đánh giá của khách bị quản trị ẩn
}
