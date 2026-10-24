# OldBook — Website quản lý chuỗi cửa hàng sách cũ

Đề tài cuối kỳ môn Lập trình Web: hệ thống bán lẻ đa chi nhánh cho chuỗi cửa hàng sách cũ, gồm ba mảng nghiệp vụ: **bán sách online**, **thu mua sách cũ từ khách** và **vận hành liên chi nhánh** (kho, chuyển kho, báo cáo toàn chuỗi).

**Công nghệ:** Spring Boot 3.3 · Spring MVC + JSP/JSTL · Spring Security (đăng nhập session cho trang web, JWT cho `/api/**`) · Spring Data JPA/Hibernate · SQL Server · WebSocket (STOMP qua SockJS) · Cloudinary · Apache POI và OpenPDF (xuất báo cáo) · Chart.js · Bootstrap 5.3 và Tailwind (CDN) · JUnit 5 + Mockito.

Nhóm 2 người, chia theo chiều dọc (mỗi người làm trọn một cụm chức năng từ dữ liệu đến giao diện). Khung sườn ban đầu và phân công nằm trong [`Khung_suon_du_an.docx`](./Khung_suon_du_an.docx).

## Chức năng

| Phân hệ | Nội dung | Phụ trách |
|---|---|---|
| Xác thực và phân quyền | Đăng ký, đăng nhập; 4 vai trò `ADMIN`, `MANAGER`, `STAFF`, `CUSTOMER`; JWT cho REST | A |
| Người dùng, chi nhánh | CRUD người dùng (chỉ `ADMIN`), CRUD chi nhánh | A |
| Sách và kho | Đầu sách, bản sách theo từng chi nhánh, tìm kiếm, lọc; ảnh bìa qua Cloudinary | A |
| Thu mua sách cũ | Khách gửi yêu cầu từ trang chủ; nhân viên định giá, duyệt, hoàn tất thì nhập kho | A |
| Chuyển kho | Phiếu chuyển giữa hai chi nhánh, theo dõi trạng thái, xuất PDF | A |
| Báo cáo và xuất file | Dashboard vận hành, báo cáo theo kỳ, xuất Excel và PDF | A (+ doanh thu: B) |
| Trang khách | Chi tiết sách, đánh giá, yêu thích, tài khoản | B |
| Giỏ hàng, đặt hàng, thanh toán | Giữ chỗ bản sách nguyên tử, tách đơn theo chi nhánh, COD hoặc chuyển khoản | B |
| Mã giảm giá | Phần trăm hoặc cố định, theo chi nhánh hoặc toàn chuỗi, giới hạn lượt, mỗi khách một lần | B |
| Quản lý đơn hàng | Nhân viên xác nhận, giao, hoàn tất, huỷ; ghi nhận thanh toán; kiểm duyệt đánh giá | B |
| Thông báo realtime | Nhân viên: đơn mới, chat mới. Khách: đơn đổi trạng thái, sách yêu thích có hàng lại, thu mua được xử lý | B |
| Chat hỗ trợ | Khách chat với nhân viên từng chi nhánh qua WebSocket | B |
| Doanh thu | Dashboard và báo cáo doanh thu bán hàng online (đơn hoàn tất, theo ngày hoàn tất) | B |

### Các trang chính

| Đường dẫn | Ai dùng | Ghi chú |
|---|---|---|
| `/`, `/books/{id}`, `/login`, `/register` | Mọi người | Trang chủ có danh mục sách và form gửi yêu cầu thu mua |
| `/cart`, `/checkout`, `/orders`, `/wishlist`, `/account`, `/notifications`, `/chat` | Người đã đăng nhập | Phía khách hàng |
| `/admin/dashboard`, `/admin/reports` | `STAFF` (chi nhánh mình), `MANAGER`, `ADMIN` | Số liệu theo phạm vi vai trò |
| `/admin/books`, `/admin/book-copies`, `/admin/buybacks`, `/admin/transfers`, `/admin/orders`, `/admin/chat` | Nhân viên trở lên | `STAFF` chỉ thấy dữ liệu của chi nhánh mình |
| `/admin/stores`, `/admin/promotions`, `/admin/reviews` | `MANAGER`, `ADMIN` | |
| `/admin/users` | `ADMIN` | |

## Kiến trúc

```
Trình duyệt (JSP + JS/AJAX + SockJS/STOMP)
   │  session + CSRF                 │  Bearer JWT            │  WebSocket /ws
   ▼                                 ▼                        ▼
Controller (web/, api/) ──► Service ──► Repository (Spring Data JPA) ──► SQL Server
                              │
                              └── SimpMessagingTemplate ──► kênh STOMP (đẩy sau khi commit)
```

- Tầng: `controller → service (interface + impl) → repository`, dữ liệu trao đổi bằng DTO.
- **Hai cơ chế xác thực:** trang JSP dùng đăng nhập session (form `/login`) và CSRF; REST `/api/**` không dùng session, xác thực bằng `Authorization: Bearer <token>`. Các lệnh AJAX của trang khách (giỏ hàng, yêu thích, chat, thông báo) dùng chính phiên đăng nhập và CSRF của trang vì cùng nguồn gốc.
- **Phân quyền theo chi nhánh:** `STAFF` bị khoá vào chi nhánh được gán; `MANAGER`/`ADMIN` xem toàn chuỗi.
- **Giữ chỗ bản sách nguyên tử:** cập nhật có điều kiện `status = 'AVAILABLE'` rồi so số dòng bị ảnh hưởng, thiếu một cuốn thì huỷ cả đơn. Hai khách tranh một cuốn chỉ một người được.
- **WebSocket** (`/ws`, SockJS). `WebSocketSubscriptionInterceptor` từ chối mặc định mọi đăng ký kênh, chỉ cho đúng người:

| Kênh | Ai nghe được |
|---|---|
| `/user/queue/notifications`, `/user/queue/chat` | Mọi người đã đăng nhập, chỉ nhận tin gửi riêng cho mình |
| `/topic/store/{id}`, `/topic/chat/store/{id}` | `STAFF` và `MANAGER` của chi nhánh đó, `ADMIN` mọi chi nhánh |
| `/topic/store/all`, `/topic/chat/all` | `MANAGER`, `ADMIN` |
| `/topic/notifications` | Nhân viên trở lên (thông báo thu mua, chuyển kho) |

- Thông báo và chat được lưu cùng transaction, khung WebSocket chỉ gửi sau khi commit.

## Cài đặt và chạy

### Yêu cầu
- **JDK 17 hoặc 21** (Lombok chưa chạy được trên JDK 26), Maven 3.9+.
- **Microsoft SQL Server** 2019 trở lên và công cụ `sqlcmd`. Dự án chỉ cấu hình driver SQL Server.
- Tài khoản [Cloudinary](https://cloudinary.com) nếu muốn tải ảnh bìa sách lên (không bắt buộc để chạy ứng dụng).
- Nên đặt dự án ở đường dẫn **không có dấu tiếng Việt** (ví dụ `C:\Projects\WEB_CK`), vì đường dẫn có dấu làm `mvn spring-boot:run` lỗi.

### 1. Tạo cơ sở dữ liệu
Script [`BookStoreChain_Init.sql`](./src/main/resources/BookStoreChain_Init.sql) tạo database `BookStoreChain` (collation `Vietnamese_CI_AS`), toàn bộ 19 bảng và dữ liệu mẫu. Script **xoá và tạo lại** các bảng nên chỉ chạy trên database dùng cho dự án.

```bash
sqlcmd -S localhost -U <tai_khoan> -P <mat_khau> -C -f 65001 -i src/main/resources/BookStoreChain_Init.sql
```
Bắt buộc có `-f 65001` (UTF-8), nếu không tiếng Việt trong dữ liệu mẫu bị lỗi font.

Dữ liệu mẫu: 4 chi nhánh, 15 người dùng, 25 đầu sách, 68 bản sách, 12 yêu cầu thu mua, 8 phiếu chuyển kho, 7 mã giảm giá, 27 đơn hàng trong 30 ngày qua (24 đã hoàn tất, 3 đang xử lý), 6 đánh giá, chat và thông báo mẫu.

### 2. Cấu hình
Đặt biến môi trường (hoặc tạo `src/main/resources/application-local.yml`, đã được `.gitignore` bỏ qua):

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `DB_USERNAME`, `DB_PASSWORD` | `sa`, `123456` | Tài khoản SQL Server (nên tạo login riêng thay vì dùng `sa`) |
| `JWT_SECRET` | một chuỗi mẫu | Khoá ký JWT; khi triển khai thật phải đổi thành chuỗi ngẫu nhiên từ 32 ký tự |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | `demo` | Thiếu thì chỉ riêng việc tải ảnh bìa không hoạt động |

Cấu hình nghiệp vụ trong `application.yml`:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `app.shop.delivery-fee` | `25000` | Phí giao hàng mỗi đơn giao tận nơi |
| `app.wishlist.restock-check-ms` | `30000` | Chu kỳ quét báo "sách yêu thích có hàng lại" |

URL kết nối nằm ở `spring.datasource.url` (mặc định `localhost:1433`, database `BookStoreChain`).

> `application.yml` đặt `ddl-auto: update`, tức Hibernate có thể tự thêm cột khi khởi động. Script SQL mới là nguồn chuẩn của lược đồ.

### 3. Chạy
```bash
mvn spring-boot:run
```
Mở http://localhost:8080. Sửa file `.jsp` cần khởi động lại ứng dụng để thấy thay đổi.

### Tài khoản mẫu

| Vai trò | Tên đăng nhập | Mật khẩu | Chi nhánh |
|---|---|---|---|
| `ADMIN` | `admin` | `admin123` | toàn chuỗi |
| `MANAGER` | `manager`, `manager_dn` | `manager123` | xem script SQL |
| `STAFF` | `staff`, `staff_hcm`, `staff_dn`, `staff_center` | `staff123` | mỗi người một chi nhánh |
| `CUSTOMER` | `customer`, `khachhang`, `hoangnam`, `lananh`, `minhduc`, `thuyle`, `quanghuy`, `ngocmai` | `customer123` | không |

Đây là dữ liệu trình diễn cục bộ, không dùng cho môi trường thật.

## Kiểm thử

```bash
mvn test
```
Chạy các test đơn vị (Mockito, không cần database) và các bài quét JSP: thoát HTML dữ liệu người dùng (chống XSS), khai báo taglib, từ khoá EL dùng làm tên thuộc tính.

Các bài tích hợp ghi vào database dùng chung nên mặc định bị bỏ qua; bật bằng biến `RUN_DB_TESTS=true` và trỏ `DB_USERNAME`, `DB_PASSWORD` tới database đã chạy script. Mỗi bài tự dọn dữ liệu của mình:

| Lớp | Kiểm tra |
|---|---|
| `OrderConcurrencyIT` | Hai khách cùng đặt một cuốn qua 30 vòng: không bán trùng, không deadlock |
| `PromotionConcurrencyIT` | Nhiều khách tranh lượt dùng cuối của một mã |
| `ChatServiceIT` | Tin đồng thời không mất số chưa đọc, không tạo trùng cuộc trò chuyện |
| `SalesStatsServiceIT` | Truy vấn doanh thu khớp SQL thuần |

```bash
RUN_DB_TESTS=true mvn test -Dtest=OrderConcurrencyIT
```

## Cấu trúc thư mục

```
src/main/java/com/bookstorechain/
├── config/        SecurityConfig, WebSocketConfig, CloudinaryConfig, ClockConfig, SchedulingConfig, DataInitializer
├── controller/
│   ├── web/       trả về JSP (khách và quản trị) và các điểm cuối AJAX
│   └── api/       REST bảo vệ bằng JWT
├── service/       giao diện nghiệp vụ; impl/ là cài đặt
├── repository/    Spring Data JPA
├── entity/        mô hình dữ liệu
├── dto/           request/ và response/
├── enums/         trạng thái, vai trò, loại thông báo...
├── security/      JWT, UserDetails, kiểm soát đăng ký kênh WebSocket
├── scheduler/     tác vụ định kỳ (báo sách có hàng lại)
└── exception/     BusinessException, ResourceNotFoundException...
src/main/webapp/WEB-INF/views/   JSP: index, auth/, customer/, admin/
src/main/resources/              application.yml, BookStoreChain_Init.sql, static/js/
src/test/                        test đơn vị, test tích hợp, bài quét JSP
docs/                            tài liệu nộp bài
```

## Quy trình Git
- Mỗi cụm chức năng một nhánh `feature/...`, commit theo dạng `loai(pham-vi): mo ta`.
- Phần của thành viên B được chia thành 8 Pull Request xếp chồng (mỗi PR nhắm vào nhánh của PR trước) để thành viên A review chéo theo từng giai đoạn; merge theo thứ tự.

## Hạn chế đã biết
- Thanh toán là COD hoặc chuyển khoản do nhân viên xác nhận thủ công, chưa tích hợp cổng thanh toán hay mã QR.
- Ảnh tình trạng sách khi thu mua chưa được tải lên từ form của khách (trường dữ liệu đã có).
- Đăng ký tài khoản chưa kiểm tra dữ liệu đầu vào; chuỗi `JWT_SECRET` mặc định chỉ để chạy thử.
- Giao diện dùng cả Bootstrap 5.3 và Tailwind (CDN), cần kết nối mạng để tải thư viện, phông chữ và biểu đồ.
