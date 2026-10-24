import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Vẽ sơ đồ hoạt động có làn bơi (swimlane) từ file mô tả văn bản.
 *
 * Chạy: java DrawDiagram.java src/dat-hang.txt dat-hang.png
 *
 * Cú pháp file mô tả (mỗi dòng một mục, dấu | ngăn cách):
 *   title|Tiêu đề (không vẽ, chỉ để ghi chú)
 *   lane|Tên làn
 *   n|id|chỉ số làn (từ 0)|hàng (từ 0)|loại|nội dung      loại: s = bắt đầu, e = kết thúc, a = hành động, d = rẽ nhánh
 *   e|từ|đến|nhãn|kiểu                                     kiểu: v = đi xuống (mặc định), h = rời từ cạnh bên
 */
public class DrawDiagram {

    static final int LANE_W = 330, HEADER_H = 46, ROW_H = 104, NODE_W = 262, NODE_H = 66, DIAMOND_H = 88, MARGIN = 14;
    static final int SCALE = 2;

    record Node(String id, int lane, int row, String type, String text) {
        int cx() { return MARGIN + lane * LANE_W + LANE_W / 2; }
        int cy() { return MARGIN + HEADER_H + row * ROW_H + ROW_H / 2; }
        int w() { return type.equals("s") || type.equals("e") ? 28 : NODE_W; }
        int h() { return type.equals("s") || type.equals("e") ? 28 : type.equals("d") ? DIAMOND_H : NODE_H; }
        int top() { return cy() - h() / 2; }
        int bottom() { return cy() + h() / 2; }
        int left() { return cx() - w() / 2; }
        int right() { return cx() + w() / 2; }
    }

    record Edge(String from, String to, String label, String mode) { }

    public static void main(String[] args) throws Exception {
        List<String> lanes = new ArrayList<>();
        Map<String, Node> nodes = new HashMap<>();
        List<Edge> edges = new ArrayList<>();
        for (String line : Files.readAllLines(new File(args[0]).toPath(), StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\\|", -1);
            switch (p[0]) {
                case "lane" -> lanes.add(p[1]);
                case "n" -> nodes.put(p[1], new Node(p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]), p[4], p[5]));
                case "e" -> edges.add(new Edge(p[1], p[2], p.length > 3 ? p[3] : "", p.length > 4 && !p[4].isEmpty() ? p[4] : "v"));
                default -> { }
            }
        }
        int rows = nodes.values().stream().mapToInt(Node::row).max().orElse(0) + 1;
        int width = MARGIN * 2 + lanes.size() * LANE_W;
        int height = MARGIN * 2 + HEADER_H + rows * ROW_H;

        BufferedImage img = new BufferedImage(width * SCALE, height * SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.scale(SCALE, SCALE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        Font body = new Font("Arial", Font.PLAIN, 14);
        Font bold = new Font("Arial", Font.BOLD, 15);

        // làn bơi
        for (int i = 0; i < lanes.size(); i++) {
            int x = MARGIN + i * LANE_W;
            g.setColor(i % 2 == 0 ? new Color(0xF7F8FA) : Color.WHITE);
            g.fillRect(x, MARGIN, LANE_W, HEADER_H + rows * ROW_H);
            g.setColor(new Color(0xE3E6EB));
            g.fillRect(x, MARGIN, LANE_W, HEADER_H);
            g.setColor(new Color(0x555B66));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRect(x, MARGIN, LANE_W, HEADER_H + rows * ROW_H);
            g.drawLine(x, MARGIN + HEADER_H, x + LANE_W, MARGIN + HEADER_H);
            g.setFont(bold);
            g.setColor(new Color(0x111111));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(lanes.get(i), x + (LANE_W - fm.stringWidth(lanes.get(i))) / 2, MARGIN + HEADER_H / 2 + fm.getAscent() / 2 - 2);
        }

        // mũi tên trước, nút vẽ đè lên
        g.setFont(body);
        for (Edge e : edges) drawEdge(g, nodes.get(e.from), nodes.get(e.to), e);
        for (Node n : nodes.values()) drawNode(g, n, body);

        g.dispose();
        ImageIO.write(img, "png", new File(args[1]));
        System.out.println(args[1] + " " + img.getWidth() + "x" + img.getHeight());
    }

    static void drawNode(Graphics2D g, Node n, Font body) {
        g.setStroke(new BasicStroke(1.5f));
        switch (n.type) {
            case "s" -> {
                g.setColor(Color.BLACK);
                g.fill(new Ellipse2D.Double(n.left(), n.top(), n.w(), n.h()));
            }
            case "e" -> {
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(n.left(), n.top(), n.w(), n.h()));
                g.setColor(Color.BLACK);
                g.draw(new Ellipse2D.Double(n.left(), n.top(), n.w(), n.h()));
                g.fill(new Ellipse2D.Double(n.left() + 6, n.top() + 6, n.w() - 12, n.h() - 12));
            }
            case "d" -> {
                Polygon d = new Polygon(
                        new int[]{n.cx(), n.right(), n.cx(), n.left()},
                        new int[]{n.top(), n.cy(), n.bottom(), n.cy()}, 4);
                g.setColor(new Color(0xFFF4CC));
                g.fill(d);
                g.setColor(new Color(0x222222));
                g.draw(d);
                drawText(g, body, n.text, n.cx(), n.cy(), 150);
            }
            default -> {
                RoundRectangle2D r = new RoundRectangle2D.Double(n.left(), n.top(), n.w(), n.h(), 14, 14);
                g.setColor(new Color(0xE8F0FB));
                g.fill(r);
                g.setColor(new Color(0x222222));
                g.draw(r);
                drawText(g, body, n.text, n.cx(), n.cy(), n.w() - 22);
            }
        }
    }

    static void drawText(Graphics2D g, Font font, String text, int cx, int cy, int maxW) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.split(" ")) {
            String next = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(next) > maxW && cur.length() > 0) {
                lines.add(cur.toString());
                cur = new StringBuilder(word);
            } else {
                cur = new StringBuilder(next);
            }
        }
        lines.add(cur.toString());
        int lh = fm.getHeight() - 2;
        int y = cy - (lines.size() * lh) / 2 + fm.getAscent() - 1;
        g.setColor(new Color(0x111111));
        for (String line : lines) {
            g.drawString(line, cx - fm.stringWidth(line) / 2, y);
            y += lh;
        }
    }

    static void drawEdge(Graphics2D g, Node a, Node b, Edge e) {
        g.setColor(new Color(0x222222));
        g.setStroke(new BasicStroke(1.5f));
        List<int[]> pts = new ArrayList<>();
        int labelX, labelY;
        if (e.mode.equals("h")) {
            boolean right = b.cx() > a.cx();
            int sx = right ? a.right() : a.left();
            int sy = a.cy();
            pts.add(new int[]{sx, sy});
            if (a.row == b.row) {
                pts.add(new int[]{right ? b.left() : b.right(), b.cy()});
            } else {
                pts.add(new int[]{b.cx(), sy});
                pts.add(new int[]{b.cx(), b.row > a.row ? b.top() : b.bottom()});
            }
            labelX = sx + (right ? 6 : -6);
            labelY = sy - 6;
            drawPath(g, pts);
            drawLabel(g, e.label, labelX, labelY, right);
            return;
        }
        int sx = a.cx(), sy = a.bottom(), tx = b.cx(), ty = b.top();
        pts.add(new int[]{sx, sy});
        if (sx != tx) {
            int mid = (sy + ty) / 2;
            pts.add(new int[]{sx, mid});
            pts.add(new int[]{tx, mid});
        }
        pts.add(new int[]{tx, ty});
        drawPath(g, pts);
        drawLabel(g, e.label, sx + 6, sy + 16, true);
    }

    static void drawPath(Graphics2D g, List<int[]> pts) {
        for (int i = 0; i + 1 < pts.size(); i++) {
            g.drawLine(pts.get(i)[0], pts.get(i)[1], pts.get(i + 1)[0], pts.get(i + 1)[1]);
        }
        int[] p = pts.get(pts.size() - 1), q = pts.get(pts.size() - 2);
        double ang = Math.atan2(p[1] - q[1], p[0] - q[0]);
        int len = 9;
        Polygon head = new Polygon();
        head.addPoint(p[0], p[1]);
        head.addPoint((int) Math.round(p[0] - len * Math.cos(ang - 0.4)), (int) Math.round(p[1] - len * Math.sin(ang - 0.4)));
        head.addPoint((int) Math.round(p[0] - len * Math.cos(ang + 0.4)), (int) Math.round(p[1] - len * Math.sin(ang + 0.4)));
        g.fill(head);
    }

    static void drawLabel(Graphics2D g, String label, int x, int y, boolean leftAligned) {
        if (label == null || label.isEmpty()) return;
        g.setFont(new Font("Arial", Font.ITALIC, 13));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(label);
        int lx = leftAligned ? x : x - w;
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(lx - 2, y - fm.getAscent(), w + 4, fm.getHeight() - 2);
        g.setColor(new Color(0x333333));
        g.drawString(label, lx, y);
        g.setColor(new Color(0x222222));
    }
}
