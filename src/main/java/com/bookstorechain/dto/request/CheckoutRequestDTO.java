package com.bookstorechain.dto.request;

import com.bookstorechain.enums.FulfillmentType;
import com.bookstorechain.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CheckoutRequestDTO {

    @NotBlank(message = "Vui lòng nhập họ tên người nhận")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String receiverName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^[0-9+ ]{9,15}$", message = "Số điện thoại không hợp lệ")
    private String receiverPhone;

    @NotNull(message = "Vui lòng chọn hình thức nhận hàng")
    @Builder.Default
    private FulfillmentType fulfillmentType = FulfillmentType.PICKUP;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String shippingAddress;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
