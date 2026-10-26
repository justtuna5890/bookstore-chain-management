package com.bookstorechain.service.impl;

import org.springframework.stereotype.Component;

/**
 * Khoá theo người dùng để tuần tự hoá các thao tác ghi của cùng một khách (thêm giỏ, đặt hàng).
 * Tránh yêu cầu song song cùng tạo giỏ hàng hoặc cùng đặt một bản sách gây deadlock/vi phạm ràng buộc trên SQL Server.
 * Dùng mảng khoá cố định nên không tăng bộ nhớ theo số người dùng; chỉ đúng với một instance ứng dụng.
 */
@Component
public class UserLocks {

    private static final int STRIPES = 64;
    private final Object[] locks = new Object[STRIPES];

    public UserLocks() {
        for (int i = 0; i < STRIPES; i++) {
            locks[i] = new Object();
        }
    }

    public Object lockFor(Long userId) {
        return locks[(int) (userId % STRIPES)];
    }
}
