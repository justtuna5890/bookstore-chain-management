package com.bookstorechain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeRequestDTO {

    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
    private String currentPassword;

    // BCrypt chỉ dùng 72 byte đầu nên giới hạn độ dài để mật khẩu dài không bị cắt âm thầm
    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 6, max = 64, message = "Mật khẩu mới từ 6 đến 64 ký tự")
    private String newPassword;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới")
    private String confirmPassword;
}
