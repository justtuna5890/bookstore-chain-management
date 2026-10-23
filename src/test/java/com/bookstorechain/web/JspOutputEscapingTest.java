package com.bookstorechain.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EL của JSP không tự thoát HTML nên in thẳng ${...} một chuỗi do người dùng nhập là lỗ hổng XSS lưu trữ
 * (khách nhập mã HTML vào tên người nhận, nhân viên mở trang đơn hàng thì mã chạy trên trình duyệt của nhân viên).
 * Test này quét các trang khách hàng và các trang quản trị bán hàng, báo lỗi nếu in thô các thuộc tính chuỗi
 * thường chứa dữ liệu người dùng; phải bọc bằng fn:escapeXml(...) hoặc dùng c:out.
 */
class JspOutputEscapingTest {

    private static final Path VIEWS = Paths.get("src/main/webapp/WEB-INF/views");

    private static final List<String> SCANNED = List.of("customer", "admin/order", "admin/promotion", "admin/review", "admin/common/page-top.jsp");

    /** Biểu thức in thô một thuộc tính (hoặc biến) có tên thường mang chữ do người dùng nhập. */
    private static final Pattern RAW_TEXT = Pattern.compile(
            "\\$\\{\\s*(?:[\\w]+\\.)*(?:title|author|category|coverImage|publisher|description|copyCode|storeName|storeAddress|storePhone|"
                    + "note|receiverName|receiverPhone|shippingAddress|customerName|promotionCode|transactionCode|code|comment|reviewerName|"
                    + "fullName|username|email|phone|name|message|err|bookTitle|orderCode|"
                    + "cartNotice|checkoutError|orderSuccess|orderError|successMessage|errorMessage|selectedKeyword|currentStoreName)\\s*\\}");

    @Test
    @DisplayName("Các trang khách và trang quản trị bán hàng không in thô chuỗi do người dùng nhập")
    void noRawUserTextInJsp() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (String scanned : SCANNED) {
            Path root = VIEWS.resolve(scanned);
            if (!Files.exists(root)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".jsp"))::iterator) {
                    List<String> lines = Files.readAllLines(file);
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        if (line.contains("<c:out ") || line.contains("<%--")) {
                            continue;
                        }
                        Matcher matcher = RAW_TEXT.matcher(line);
                        if (matcher.find()) {
                            offenders.add(VIEWS.relativize(file) + ":" + (i + 1) + "  " + matcher.group());
                        }
                    }
                }
            }
        }
        assertThat(offenders).as("biểu thức in thô cần bọc fn:escapeXml").isEmpty();
    }

    @Test
    @DisplayName("Bộ quét phát hiện đúng mẫu in thô và bỏ qua mẫu đã thoát")
    void scannerRecognisesRawAndEscapedForms() {
        assertThat(RAW_TEXT.matcher("<td>${order.customerName}</td>").find()).isTrue();
        assertThat(RAW_TEXT.matcher("value=\"${form.receiverName}\"").find()).isTrue();
        assertThat(RAW_TEXT.matcher("<td>${fn:escapeXml(order.customerName)}</td>").find()).isFalse();
        assertThat(RAW_TEXT.matcher("<td>${order.itemCount}</td>").find()).isFalse();
    }
}
