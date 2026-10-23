package com.bookstorechain.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Thời điểm tương đối kiểu "5 phút trước" dùng cho thông báo và chat. */
final class RelativeTime {

    private RelativeTime() {
    }

    static String ago(LocalDateTime time, LocalDateTime now) {
        if (time == null) {
            return "";
        }
        Duration age = Duration.between(time, now);
        if (age.toMinutes() < 1) {
            return "Vừa xong";
        }
        if (age.toMinutes() < 60) {
            return age.toMinutes() + " phút trước";
        }
        if (age.toHours() < 24) {
            return age.toHours() + " giờ trước";
        }
        if (age.toDays() < 7) {
            return age.toDays() + " ngày trước";
        }
        return time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
