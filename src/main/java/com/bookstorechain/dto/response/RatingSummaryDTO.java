package com.bookstorechain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingSummaryDTO {
    private Long bookId;
    /** Điểm trung bình 0-5; 0 nếu chưa có đánh giá. */
    private double average;
    private int count;
    /** Thanh phân bố từ 5 sao xuống 1 sao; rỗng khi chỉ cần điểm trung bình (danh sách sách). */
    @Builder.Default
    private List<StarBar> bars = new ArrayList<>();

    public boolean isRated() {
        return count > 0;
    }

    /** Điểm trung bình làm tròn một chữ số, VD "4.5"; "—" nếu chưa có đánh giá. */
    public String getAverageLabel() {
        return count == 0 ? "—" : String.format(Locale.US, "%.1f", average);
    }

    /** Phần trăm độ rộng lớp sao vàng khi vẽ 5 sao. */
    public int getStarPercent() {
        return (int) Math.round(average / 5.0 * 100);
    }

    @Getter
    @AllArgsConstructor
    public static class StarBar {
        private final int star;
        private final int count;
        private final int percent;
    }
}
