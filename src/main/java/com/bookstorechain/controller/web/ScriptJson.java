package com.bookstorechain.controller.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/** JSON để nhúng thẳng vào thẻ script của JSP. */
@Slf4j
final class ScriptJson {

    private ScriptJson() {
    }

    /**
     * Thoát dấu '<' (thành <, JavaScript vẫn đọc ra đúng ký tự) để chuỗi chứa "</script>" như tên chi nhánh
     * không đóng sớm thẻ script và chèn được mã vào trang.
     */
    static String of(ObjectMapper objectMapper, Object value, String fallback) {
        try {
            return objectMapper.writeValueAsString(value).replace("<", "\\u003c");
        } catch (JsonProcessingException e) {
            log.error("Lỗi chuyển đổi dữ liệu sang JSON", e);
            return fallback;
        }
    }
}
