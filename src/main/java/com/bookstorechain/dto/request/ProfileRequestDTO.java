package com.bookstorechain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ProfileRequestDTO {

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    private String fullName;

    /** Có thể để trống; nếu nhập thì cùng quy tắc với số điện thoại người nhận khi đặt hàng. */
    @Pattern(regexp = "^$|^[0-9+ ]{9,15}$", message = "Số điện thoại không hợp lệ")
    private String phone;
}
