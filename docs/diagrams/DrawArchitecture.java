import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/** Vẽ sơ đồ kiến trúc phân tầng. Chạy: java DrawArchitecture.java kien-truc.png */
public class DrawArchitecture {
    static final int W = 1100, H = 915, S = 2;
    static Graphics2D g;

    public static void main(String[] a) throws Exception {
        BufferedImage img = new BufferedImage(W * S, H * S, BufferedImage.TYPE_INT_RGB);
        g = img.createGraphics();
        g.scale(S, S);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);

        box(60, 20, 980, 90, "Trình duyệt", "JSP/JSTL do máy chủ dựng, Bootstrap 5.3 + Tailwind, JavaScript (fetch, SockJS + STOMP, Chart.js)", 0xEEF3FA);
        arrow(550, 110, 550, 150);
        box(60, 150, 980, 100, "Spring Security", "Chuỗi lọc: đăng nhập session + CSRF cho trang web | JwtAuthenticationFilter cho /api/** | WebSocketSubscriptionInterceptor chặn đăng ký kênh STOMP trái phép", 0xFDF1E3);
        arrow(550, 250, 550, 290);
        box(60, 290, 980, 90, "Controller", "controller/web: trả về JSP và các điểm cuối AJAX    |    controller/api: REST, trả về JSON", 0xEEF3FA);
        arrow(550, 380, 550, 420);
        box(60, 420, 640, 130, "Service (giao diện + cài đặt)", "Quy tắc nghiệp vụ, @Transactional, khoá theo từng người dùng (UserLocks), phân quyền theo chi nhánh, tính doanh thu, xuất Excel/PDF", 0xEAF6EC);
        box(720, 420, 320, 130, "Thông báo realtime", "UserNotificationService lưu cùng transaction; AfterCommit gửi khung qua SimpMessagingTemplate tới /topic và /user/queue", 0xF4EAF7);
        arrow(380, 550, 380, 590);
        box(60, 590, 640, 90, "Repository (Spring Data JPA)", "Truy vấn dẫn xuất, JPQL, UPDATE có điều kiện (giữ chỗ bản sách, trừ lượt mã), khoá bi quan lockById", 0xEAF6EC);
        arrow(380, 680, 380, 720);
        box(60, 720, 640, 90, "SQL Server: BookStoreChain", "19 bảng, collation Vietnamese_CI_AS, script BookStoreChain_Init.sql tạo lược đồ và dữ liệu mẫu", 0xEDEDED);
        box(720, 640, 320, 80, "Cloudinary", "Lưu ảnh bìa sách", 0xEEF3FA);
        box(720, 740, 320, 70, "Apache POI, OpenPDF", "Xuất báo cáo Excel và PDF", 0xEEF3FA);
        box(60, 830, 980, 66, "Tác vụ định kỳ", "WishlistRestockScheduler quét sách yêu thích có hàng lại (mặc định 30 giây)", 0xF7F7F7);
        g.dispose();
        ImageIO.write(img, "png", new File(a[0]));
    }

    static void box(int x, int y, int w, int h, String title, String text, int rgb) {
        g.setColor(new Color(rgb));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        g.setColor(new Color(0x222222));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString(title, x + 14, y + 25);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.split(" ")) {
            String next = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(next) > w - 28 && cur.length() > 0) { lines.add(cur.toString()); cur = new StringBuilder(word); }
            else cur = new StringBuilder(next);
        }
        lines.add(cur.toString());
        int y2 = y + 48;
        for (String l : lines) { g.drawString(l, x + 14, y2); y2 += 18; }
    }

    static void arrow(int x1, int y1, int x2, int y2) {
        g.setColor(new Color(0x222222));
        g.setStroke(new BasicStroke(1.6f));
        g.drawLine(x1, y1, x2, y2);
        Polygon p = new Polygon();
        p.addPoint(x2, y2);
        p.addPoint(x2 - 5, y2 - 10);
        p.addPoint(x2 + 5, y2 - 10);
        g.fill(p);
    }
}
