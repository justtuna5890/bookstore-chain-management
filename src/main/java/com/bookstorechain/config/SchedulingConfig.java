package com.bookstorechain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Bật các tác vụ định kỳ (@Scheduled), hiện dùng để báo sách yêu thích có hàng lại. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
