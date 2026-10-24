package com.bookstorechain.service;

import com.bookstorechain.dto.request.PasswordChangeRequestDTO;
import com.bookstorechain.dto.request.ProfileRequestDTO;
import com.bookstorechain.dto.response.ProfileDTO;

/** Tài khoản cá nhân của người đang đăng nhập: xem, sửa thông tin liên hệ và đổi mật khẩu. */
public interface AccountService {

    ProfileDTO getProfile(Long userId);

    /** Sửa họ tên và số điện thoại; tên đăng nhập và email không đổi được. Trả về thông tin sau khi lưu. */
    ProfileDTO updateProfile(Long userId, ProfileRequestDTO request);

    /**
     * Đổi mật khẩu sau khi xác minh mật khẩu hiện tại. Trả về mật khẩu mới đã băm để đồng bộ vào phiên đăng nhập
     * đang giữ bản sao người dùng.
     */
    String changePassword(Long userId, PasswordChangeRequestDTO request);
}
