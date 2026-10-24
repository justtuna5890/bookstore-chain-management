package com.bookstorechain.exception;

/** Lỗi vi phạm quy tắc nghiệp vụ (ví dụ sách đã bị khách khác đặt), thông báo an toàn để hiển thị cho người dùng. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
