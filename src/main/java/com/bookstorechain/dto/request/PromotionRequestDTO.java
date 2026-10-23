package com.bookstorechain.dto.request;

import com.bookstorechain.enums.PromotionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRequestDTO {

    private static final String INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm";

    @NotBlank(message = "Vui lòng nhập mã giảm giá")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,30}$", message = "Mã gồm 3-30 ký tự chữ, số, gạch ngang hoặc gạch dưới")
    private String code;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @NotNull(message = "Vui lòng chọn loại giảm giá")
    @Builder.Default
    private PromotionType type = PromotionType.PERCENT;

    @NotNull(message = "Vui lòng nhập mức giảm")
    @DecimalMin(value = "0.01", message = "Mức giảm phải lớn hơn 0")
    private BigDecimal value;

    @DecimalMin(value = "0", message = "Đơn tối thiểu không được âm")
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0.01", message = "Mức giảm tối đa phải lớn hơn 0")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Vui lòng chọn thời điểm bắt đầu")
    @DateTimeFormat(pattern = INPUT_PATTERN)
    private LocalDateTime startsAt;

    @NotNull(message = "Vui lòng chọn thời điểm kết thúc")
    @DateTimeFormat(pattern = INPUT_PATTERN)
    private LocalDateTime endsAt;

    @Min(value = 1, message = "Giới hạn lượt dùng phải từ 1 trở lên (để trống nếu không giới hạn)")
    private Integer usageLimit;

    /** null là áp dụng toàn chuỗi. */
    private Long storeId;

    private boolean onePerCustomer;

    private boolean active;

    /** Giá trị cho ô datetime-local của form. */
    public String getStartsAtInput() {
        return startsAt == null ? "" : startsAt.format(DateTimeFormatter.ofPattern(INPUT_PATTERN));
    }

    public String getEndsAtInput() {
        return endsAt == null ? "" : endsAt.format(DateTimeFormatter.ofPattern(INPUT_PATTERN));
    }
}
