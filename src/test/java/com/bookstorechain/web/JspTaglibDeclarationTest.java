package com.bookstorechain.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSP chỉ biên dịch khi người dùng mở trang, nên dùng tiền tố thẻ (fn:, fmt:, c:) mà quên khai báo taglib chỉ lộ ra
 * lúc chạy bằng lỗi 500. Test này bắt lỗi đó ngay khi build.
 */
class JspTaglibDeclarationTest {

    private static final Path VIEWS = Paths.get("src/main/webapp/WEB-INF/views");

    private static final List<String> PREFIXES = List.of("c", "fn", "fmt", "sec", "form");

    @Test
    @DisplayName("Mọi JSP dùng tiền tố thẻ đều khai báo taglib tương ứng")
    void everyUsedPrefixIsDeclared() throws IOException {
        List<String> problems = new ArrayList<>();
        try (Stream<Path> files = Files.walk(VIEWS)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".jsp"))::iterator) {
                String content = Files.readString(file);
                for (String prefix : PREFIXES) {
                    boolean used = usesPrefix(content, prefix);
                    boolean declared = Pattern.compile("<%@\\s*taglib\\s+prefix=\"" + prefix + "\"").matcher(content).find();
                    if (used && !declared) {
                        problems.add(VIEWS.relativize(file) + " dùng '" + prefix + ":' nhưng chưa khai báo taglib");
                    }
                }
            }
        }
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("Bộ quét nhận ra cả thẻ lẫn hàm EL và bỏ qua chữ thường gặp như URL hay giờ")
    void scannerRecognisesUsage() {
        assertThat(usesPrefix("<c:if test=\"${x}\"></c:if>", "c")).isTrue();
        assertThat(usesPrefix("${fn:escapeXml(a)}", "fn")).isTrue();
        assertThat(usesPrefix("${not empty a ? fn:escapeXml(a) : 'x'}", "fn")).isTrue();
        assertThat(usesPrefix("<a href=\"https://x.vn\">12:30</a> style=\"fn:1\"", "fn")).isFalse();
    }

    /** Thẻ dạng &lt;prefix:tag hoặc hàm EL dạng prefix:ten( bên trong ${...}. */
    private static boolean usesPrefix(String content, String prefix) {
        return Pattern.compile("</?" + prefix + ":[A-Za-z]").matcher(content).find()
                || Pattern.compile("\\$\\{[^}]*\\b" + prefix + ":[A-Za-z]+\\(").matcher(content).find();
    }
}
