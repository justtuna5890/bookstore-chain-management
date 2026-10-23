package com.bookstorechain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Đồng hồ hệ thống dạng bean để các dịch vụ phụ thuộc thời gian (mã giảm giá) kiểm thử được bằng đồng hồ cố định. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
