# Bookstore Chain Management

Website quản lý chuỗi cửa hàng sách cũ — Spring Boot + JSP/JSTL + Bootstrap + JPA + MySQL/PostgreSQL/SQL Server + JWT + WebSocket + Cloudinary.

Xem chi tiết kiến trúc, domain model và phân chia công việc trong [`Khung_suon_du_an.docx`](./Khung_suon_du_an.docx).

## Yêu cầu môi trường
- Java 17+
- Maven 3.9+
- MySQL 8+ (mặc định; xem mục "Đổi cơ sở dữ liệu" bên dưới nếu dùng PostgreSQL/SQL Server)

## Cấu hình
1. Tạo database: `CREATE DATABASE bookstore_chain;`
2. Set biến môi trường (hoặc tạo file `src/main/resources/application-local.yml`, file này đã được `.gitignore` bỏ qua):
   - `DB_USERNAME`, `DB_PASSWORD`
   - `JWT_SECRET` — chuỗi ngẫu nhiên ít nhất 32 ký tự
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`

## Chạy ứng dụng
```bash
mvn spring-boot:run
```
Ứng dụng chạy tại http://localhost:8080

## Đổi cơ sở dữ liệu
Sửa trong `pom.xml` (đổi dependency driver) và `application.yml` (đổi `spring.datasource.url` và `driver-class-name`):
- **PostgreSQL**: dependency `org.postgresql:postgresql`, url `jdbc:postgresql://localhost:5432/bookstore_chain`
- **SQL Server**: dependency `com.microsoft.sqlserver:mssql-jdbc`, url `jdbc:sqlserver://localhost:1433;databaseName=bookstore_chain;encrypt=false`

## Cấu trúc thư mục
```
src/main/java/com/bookstorechain/
|-- config/          (SecurityConfig, WebSocketConfig, CloudinaryConfig)
|-- entity/          (User, Role, Store, Book, BookCopy)
|-- repository/
|-- security/        (JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService)
|-- controller/
|   |-- web/          (trả JSP)
|   |-- api/          (REST, bảo vệ bằng JWT)

src/main/webapp/WEB-INF/views/   (các file .jsp)
src/main/resources/application.yml
```

## Kiến trúc auth
- `/api/**` — REST, stateless, xác thực bằng JWT (`Authorization: Bearer <token>`)
- Các route còn lại — JSP, xác thực bằng session (Spring Security form-login tại `/login`)

## Trạng thái hiện tại
Đây là khung sườn khởi tạo: cấu hình Security/JWT/WebSocket/Cloudinary, 4 entity lõi (`User`, `Store`, `Book`, `BookCopy`), 1 luồng mẫu chạy được đầu-cuối (trang chủ liệt kê sách, đăng ký/đăng nhập qua API). Các entity và chức năng còn lại (BuybackRequest, StockTransfer, Cart, Order, Payment, Review, Promotion, ChatMessage, Notification) sẽ được bổ sung theo phân chia công việc trong `Khung_suon_du_an.docx`.

## Kiểm thử
Dùng JDK 17 hoặc 21 (Lombok chưa chạy được trên JDK 26).

```bash
mvn test
```
Chạy các test đơn vị (không cần database) cho giỏ hàng, đặt hàng, huỷ đơn.

Test tranh chấp trên SQL Server thật (hai khách cùng đặt một cuốn, 30 vòng) mặc định bị bỏ qua vì ghi vào database dùng chung. Chạy khi cần:
```bash
RUN_DB_TESTS=true mvn test -Dtest=OrderConcurrencyIT
```
Test dùng hai khách mẫu `customer` và `khachhang` của `BookStoreChain_Init.sql` và tự dọn đơn, giỏ, trạng thái bản sách sau mỗi vòng.

## Cấu hình cửa hàng
- `app.shop.delivery-fee`: phí giao hàng cho mỗi đơn giao tận nơi (mặc định 25000).

