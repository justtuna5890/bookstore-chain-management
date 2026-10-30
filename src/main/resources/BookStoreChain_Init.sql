-- ===================================================================================
-- SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU & DỮ LIỆU MẪU DỰ ÁN BOOKSTORE CHAIN (OLDBOOK)
-- HỆ QUẢN TRỊ CSDL: MICROSOFT SQL SERVER
-- DATABASE: BookStoreChain
-- BẢNG MÃ: Unicode (NVARCHAR, Collation: Vietnamese_CI_AS)
-- DỮ LIỆU ĐA DẠNG: Đầy đủ 4 Chi nhánh, 16 Người dùng, 25 Đầu sách, 41 Bản sách,
--                  12 Đơn thu mua & 8 Phiếu điều chuyển kho đa chiều.
-- ===================================================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'BookStoreChain')
BEGIN
    CREATE DATABASE BookStoreChain COLLATE Vietnamese_CI_AS;
END
GO

USE BookStoreChain;
GO

-- 1. XÓA BẢNG CŨ NẾU ĐÃ TỒN TẠI (THEO THỨ TỰ RÀNG BUỘC KHÓA NGOẠI)
IF OBJECT_ID(N'dbo.chat_message', N'U') IS NOT NULL DROP TABLE dbo.chat_message;
IF OBJECT_ID(N'dbo.chat_conversation', N'U') IS NOT NULL DROP TABLE dbo.chat_conversation;
IF OBJECT_ID(N'dbo.user_notification', N'U') IS NOT NULL DROP TABLE dbo.user_notification;
IF OBJECT_ID(N'dbo.wishlist_item', N'U') IS NOT NULL DROP TABLE dbo.wishlist_item;
IF OBJECT_ID(N'dbo.review', N'U') IS NOT NULL DROP TABLE dbo.review;
IF OBJECT_ID(N'dbo.payment', N'U') IS NOT NULL DROP TABLE dbo.payment;
IF OBJECT_ID(N'dbo.order_item', N'U') IS NOT NULL DROP TABLE dbo.order_item;
IF OBJECT_ID(N'dbo.customer_order', N'U') IS NOT NULL DROP TABLE dbo.customer_order;
IF OBJECT_ID(N'dbo.promotion', N'U') IS NOT NULL DROP TABLE dbo.promotion;
IF OBJECT_ID(N'dbo.cart_item', N'U') IS NOT NULL DROP TABLE dbo.cart_item;
IF OBJECT_ID(N'dbo.cart', N'U') IS NOT NULL DROP TABLE dbo.cart;
IF OBJECT_ID(N'dbo.stock_transfer_item', N'U') IS NOT NULL DROP TABLE dbo.stock_transfer_item;
IF OBJECT_ID(N'dbo.stock_transfer', N'U') IS NOT NULL DROP TABLE dbo.stock_transfer;
IF OBJECT_ID(N'dbo.buyback_item', N'U') IS NOT NULL DROP TABLE dbo.buyback_item;
IF OBJECT_ID(N'dbo.buyback_request', N'U') IS NOT NULL DROP TABLE dbo.buyback_request;
IF OBJECT_ID(N'dbo.book_copy', N'U') IS NOT NULL DROP TABLE dbo.book_copy;
IF OBJECT_ID(N'dbo.book', N'U') IS NOT NULL DROP TABLE dbo.book;
IF OBJECT_ID(N'dbo.users', N'U') IS NOT NULL DROP TABLE dbo.users;
IF OBJECT_ID(N'dbo.store', N'U') IS NOT NULL DROP TABLE dbo.store;
GO

-- 2. TẠO CÁC BẢNG (ÉP KIỂU NVARCHAR TOÀN DIỆN CHO CÁC CỘT VĂN BẢN/CHUỖI)

-- Bảng Chi Nhánh (store)
CREATE TABLE dbo.store (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    address NVARCHAR(255) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    CONSTRAINT PK_store PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_store_name UNIQUE (name)
);
GO

-- Bảng Người Dùng & Nhân Sự (users)
CREATE TABLE dbo.users (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    username NVARCHAR(50) NOT NULL,
    password NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20) NULL,
    role NVARCHAR(20) NOT NULL CHECK (role IN (N'CUSTOMER', N'STAFF', N'MANAGER', N'ADMIN', N'SHIPPER')),
    is_active BIT NOT NULL DEFAULT 1,
    store_id BIGINT NULL,
    CONSTRAINT PK_users PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_users_username UNIQUE (username),
    CONSTRAINT UQ_users_email UNIQUE (email),
    CONSTRAINT FK_users_store FOREIGN KEY (store_id) REFERENCES dbo.store(id)
);
GO

-- Bảng Đầu Sách (book)
CREATE TABLE dbo.book (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    title NVARCHAR(200) NOT NULL,
    author NVARCHAR(100) NOT NULL,
    publisher NVARCHAR(100) NULL,
    publication_year INT NULL,
    category NVARCHAR(50) NULL,
    description NVARCHAR(MAX) NULL,
    cover_price NUMERIC(12,2) NULL,
    cover_image NVARCHAR(500) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    CONSTRAINT PK_book PRIMARY KEY CLUSTERED (id ASC)
);
GO

-- Bảng Bản Sao Sách Tồn Kho (book_copy)
CREATE TABLE dbo.book_copy (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    copy_code NVARCHAR(50) NOT NULL,
    book_condition NVARCHAR(20) NOT NULL CHECK (book_condition IN (N'LIKE_NEW', N'GOOD', N'FAIR', N'POOR')),
    price NUMERIC(12,2) NOT NULL,
    status NVARCHAR(20) NOT NULL CHECK (status IN (N'AVAILABLE', N'SOLD', N'IN_TRANSIT', N'HOLDING', N'RESERVED')),
    note NVARCHAR(MAX) NULL,
    book_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    CONSTRAINT PK_book_copy PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_book_copy_code UNIQUE (copy_code),
    CONSTRAINT FK_book_copy_book FOREIGN KEY (book_id) REFERENCES dbo.book(id),
    CONSTRAINT FK_book_copy_store FOREIGN KEY (store_id) REFERENCES dbo.store(id)
);
GO

-- Bảng Yêu Cầu Thu Mua Sách Cũ (buyback_request)
CREATE TABLE dbo.buyback_request (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    status NVARCHAR(20) NOT NULL CHECK (status IN (N'PENDING', N'APPROVED', N'REJECTED', N'COMPLETED')),
    staff_note NVARCHAR(MAX) NULL,
    CONSTRAINT PK_buyback_request PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_buyback_request_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_buyback_request_store FOREIGN KEY (store_id) REFERENCES dbo.store(id)
);
GO

-- Bảng Chi Tiết Thu Mua (buyback_item)
CREATE TABLE dbo.buyback_item (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    buyback_request_id BIGINT NOT NULL,
    book_title NVARCHAR(200) NOT NULL,
    book_author NVARCHAR(100) NULL,
    proposed_condition NVARCHAR(20) NOT NULL CHECK (proposed_condition IN (N'LIKE_NEW', N'GOOD', N'FAIR', N'POOR')),
    offered_price NUMERIC(12,2) NULL,
    item_image NVARCHAR(500) NULL,
    CONSTRAINT PK_buyback_item PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_buyback_item_request FOREIGN KEY (buyback_request_id) REFERENCES dbo.buyback_request(id) ON DELETE CASCADE
);
GO

-- Bảng Điều Chuyển Kho (stock_transfer)
CREATE TABLE dbo.stock_transfer (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    sender_store_id BIGINT NOT NULL,
    receiver_store_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    status NVARCHAR(20) NOT NULL CHECK (status IN (N'PENDING', N'IN_TRANSIT', N'COMPLETED', N'CANCELLED')),
    note NVARCHAR(MAX) NULL,
    CONSTRAINT PK_stock_transfer PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_stock_transfer_sender FOREIGN KEY (sender_store_id) REFERENCES dbo.store(id),
    CONSTRAINT FK_stock_transfer_receiver FOREIGN KEY (receiver_store_id) REFERENCES dbo.store(id),
    CONSTRAINT FK_stock_transfer_creator FOREIGN KEY (creator_id) REFERENCES dbo.users(id)
);
GO

-- Bảng Chi Tiết Điều Chuyển (stock_transfer_item)
CREATE TABLE dbo.stock_transfer_item (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    stock_transfer_id BIGINT NOT NULL,
    book_copy_id BIGINT NOT NULL,
    CONSTRAINT PK_stock_transfer_item PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_stock_transfer_item_transfer FOREIGN KEY (stock_transfer_id) REFERENCES dbo.stock_transfer(id) ON DELETE CASCADE,
    CONSTRAINT FK_stock_transfer_item_copy FOREIGN KEY (book_copy_id) REFERENCES dbo.book_copy(id)
);
GO

-- Bảng Đánh Giá Sách (review): mỗi khách một đánh giá cho mỗi đầu sách, is_hidden do quản trị đặt
CREATE TABLE dbo.review (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment NVARCHAR(1000) NULL,
    is_hidden BIT NOT NULL DEFAULT 0,
    CONSTRAINT PK_review PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_review_user_book UNIQUE (user_id, book_id),
    CONSTRAINT FK_review_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_review_book FOREIGN KEY (book_id) REFERENCES dbo.book(id)
);
GO

-- Bảng Yêu Thích (wishlist_item): khách đánh dấu đầu sách muốn theo dõi
CREATE TABLE dbo.wishlist_item (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    in_stock_notified BIT NOT NULL DEFAULT 0,
    CONSTRAINT PK_wishlist_item PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_wishlist_user_book UNIQUE (user_id, book_id),
    CONSTRAINT FK_wishlist_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_wishlist_book FOREIGN KEY (book_id) REFERENCES dbo.book(id)
);
GO

-- Bảng Thông Báo Riêng (user_notification): thông báo gửi cho từng người dùng, lưu lại để xem sau
CREATE TABLE dbo.user_notification (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    user_id BIGINT NOT NULL,
    type NVARCHAR(20) NOT NULL CHECK (type IN ('ORDER', 'WISHLIST', 'BUYBACK', 'REVIEW', 'CHAT')),
    title NVARCHAR(150) NOT NULL,
    message NVARCHAR(500) NOT NULL,
    link NVARCHAR(255) NULL,
    is_read BIT NOT NULL DEFAULT 0,
    CONSTRAINT PK_user_notification PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_user_notification_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
);
GO
CREATE INDEX IX_user_notification_user_read ON dbo.user_notification (user_id, is_read, created_at DESC);
GO

-- Bảng Cuộc Trò Chuyện Hỗ Trợ (chat_conversation): mỗi cặp (khách, chi nhánh) một cuộc
CREATE TABLE dbo.chat_conversation (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    customer_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    status NVARCHAR(10) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
    last_message_at DATETIME2(6) NULL,
    last_message_preview NVARCHAR(100) NULL,
    customer_unread INT NOT NULL DEFAULT 0,
    staff_unread INT NOT NULL DEFAULT 0,
    CONSTRAINT PK_chat_conversation PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_chat_customer_store UNIQUE (customer_id, store_id),
    CONSTRAINT FK_chat_conversation_customer FOREIGN KEY (customer_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_chat_conversation_store FOREIGN KEY (store_id) REFERENCES dbo.store(id)
);
GO
CREATE INDEX IX_chat_conversation_store_last ON dbo.chat_conversation (store_id, last_message_at DESC);
GO

-- Bảng Tin Nhắn Hỗ Trợ (chat_message)
CREATE TABLE dbo.chat_message (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    from_customer BIT NOT NULL,
    content NVARCHAR(1000) NOT NULL,
    CONSTRAINT PK_chat_message PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_chat_message_conversation FOREIGN KEY (conversation_id) REFERENCES dbo.chat_conversation(id),
    CONSTRAINT FK_chat_message_sender FOREIGN KEY (sender_id) REFERENCES dbo.users(id)
);
GO
CREATE INDEX IX_chat_message_conversation_created ON dbo.chat_message (conversation_id, created_at DESC);
GO

-- Bảng Giỏ Hàng (cart): mỗi khách hàng có đúng một giỏ
CREATE TABLE dbo.cart (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT PK_cart PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_cart_user UNIQUE (user_id),
    CONSTRAINT FK_cart_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
);
GO

-- Bảng Chi Tiết Giỏ Hàng (cart_item): mỗi bản sách là duy nhất nên không có số lượng
CREATE TABLE dbo.cart_item (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    cart_id BIGINT NOT NULL,
    book_copy_id BIGINT NOT NULL,
    CONSTRAINT PK_cart_item PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_cart_item_cart_copy UNIQUE (cart_id, book_copy_id),
    CONSTRAINT FK_cart_item_cart FOREIGN KEY (cart_id) REFERENCES dbo.cart(id) ON DELETE CASCADE,
    CONSTRAINT FK_cart_item_copy FOREIGN KEY (book_copy_id) REFERENCES dbo.book_copy(id)
);
GO

-- Bảng Mã Giảm Giá (promotion): code luôn chữ hoa, store_id NULL là áp dụng toàn chuỗi
CREATE TABLE dbo.promotion (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    code NVARCHAR(30) NOT NULL,
    description NVARCHAR(255) NULL,
    type NVARCHAR(20) NOT NULL CHECK (type IN (N'PERCENT', N'FIXED')),
    value NUMERIC(12,2) NOT NULL,
    min_order_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    max_discount_amount NUMERIC(12,2) NULL,
    starts_at DATETIME2(6) NOT NULL,
    ends_at DATETIME2(6) NOT NULL,
    usage_limit INT NULL,
    used_count INT NOT NULL DEFAULT 0,
    one_per_customer BIT NOT NULL DEFAULT 1,
    store_id BIGINT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    CONSTRAINT PK_promotion PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_promotion_code UNIQUE (code),
    CONSTRAINT FK_promotion_store FOREIGN KEY (store_id) REFERENCES dbo.store(id)
);
GO

-- Bảng Đơn Hàng Của Khách (customer_order): mỗi đơn thuộc đúng một chi nhánh
CREATE TABLE dbo.customer_order (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    order_code NVARCHAR(30) NOT NULL,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    status NVARCHAR(20) NOT NULL CHECK (status IN (N'PENDING', N'CONFIRMED', N'READY_FOR_PICKUP', N'READY_FOR_DELIVERY', N'SHIPPING', N'DELIVERY_FAILED', N'COMPLETED', N'CANCELLED')),
    fulfillment_type NVARCHAR(20) NOT NULL CHECK (fulfillment_type IN (N'PICKUP', N'DELIVERY')),
    receiver_name NVARCHAR(100) NOT NULL,
    receiver_phone NVARCHAR(20) NOT NULL,
    shipping_address NVARCHAR(255) NULL,
    note NVARCHAR(MAX) NULL,
    subtotal NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    shipping_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL,
    completed_at DATETIME2(6) NULL,
    promotion_id BIGINT NULL,
    promotion_code NVARCHAR(30) NULL,
    shipper_id BIGINT NULL,
    delivery_failure_reason NVARCHAR(255) NULL,
    CONSTRAINT PK_customer_order PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_customer_order_code UNIQUE (order_code),
    CONSTRAINT FK_customer_order_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_customer_order_store FOREIGN KEY (store_id) REFERENCES dbo.store(id),
    CONSTRAINT FK_customer_order_promotion FOREIGN KEY (promotion_id) REFERENCES dbo.promotion(id),
    CONSTRAINT FK_customer_order_shipper FOREIGN KEY (shipper_id) REFERENCES dbo.users(id)
);
GO
-- Thống kê doanh thu lọc theo trạng thái và ngày hoàn tất
CREATE INDEX IX_customer_order_status_completed ON dbo.customer_order (status, completed_at);
GO

-- Bảng Chi Tiết Đơn Hàng (order_item): lưu snapshot giá và tên sách tại thời điểm đặt
CREATE TABLE dbo.order_item (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    order_id BIGINT NOT NULL,
    book_copy_id BIGINT NOT NULL,
    price_snapshot NUMERIC(12,2) NOT NULL,
    title_snapshot NVARCHAR(200) NOT NULL,
    copy_code_snapshot NVARCHAR(50) NOT NULL,
    CONSTRAINT PK_order_item PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_order_item_order FOREIGN KEY (order_id) REFERENCES dbo.customer_order(id) ON DELETE CASCADE,
    CONSTRAINT FK_order_item_copy FOREIGN KEY (book_copy_id) REFERENCES dbo.book_copy(id)
);
GO

-- Bảng Thanh Toán (payment): một đơn có đúng một bản ghi thanh toán
CREATE TABLE dbo.payment (
    id BIGINT IDENTITY(1,1) NOT NULL,
    created_at DATETIME2(6) NULL,
    updated_at DATETIME2(6) NULL,
    order_id BIGINT NOT NULL,
    method NVARCHAR(20) NOT NULL CHECK (method IN (N'COD', N'BANK_TRANSFER')),
    status NVARCHAR(20) NOT NULL CHECK (status IN (N'UNPAID', N'PAID', N'REFUNDED')),
    amount NUMERIC(12,2) NOT NULL,
    transaction_code NVARCHAR(100) NULL,
    paid_at DATETIME2(6) NULL,
    CONSTRAINT PK_payment PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_payment_order UNIQUE (order_id),
    CONSTRAINT FK_payment_order FOREIGN KEY (order_id) REFERENCES dbo.customer_order(id) ON DELETE CASCADE
);
GO

-- 3. CHÈN DỮ LIỆU MẪU ĐẦY ĐỦ CÁC VAI TRÒ & CHỨC NĂNG

-- 3.1. Dữ liệu Chi Nhánh (4 Cơ sở trải dài các miền)
SET IDENTITY_INSERT dbo.store ON;
INSERT INTO dbo.store (id, created_at, updated_at, name, address, phone, is_active) VALUES
(1, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OldBook Hà Nội — Tràng Tiền', N'Số 12 Tràng Tiền, Quận Hoàn Kiếm, Hà Nội', N'02439123456', 1),
(2, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OldBook Sài Gòn — Nguyễn Thị Minh Khai', N'Số 88 Nguyễn Thị Minh Khai, Quận 1, TP.HCM', N'02838123456', 1),
(3, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OldBook Đà Nẵng — Bạch Đằng', N'Số 45 Bạch Đằng, Quận Hải Châu, Đà Nẵng', N'02363123456', 1),
(4, DATEADD(day, -120, SYSDATETIME()), SYSDATETIME(), N'OldBook Cần Thơ — Ninh Kiều', N'Số 120 Hai Bà Trưng, Bến Ninh Kiều, TP. Cần Thơ', N'02923123456', 1);
SET IDENTITY_INSERT dbo.store OFF;
GO

-- 3.2. Dữ liệu Người Dùng (Đủ 5 Roles, Phân bổ theo Store, Đăng ký rải rác theo chu kỳ)
-- Mật khẩu mặc định:
-- admin: admin123 ($2a$10$u5O0YAExfMSe/tPj5YQuHuLJTeHT00rvl/hLLMbRRDqeBu5QBcPoW)
-- manager: manager123 ($2a$10$2cUgd7RJbkKPT1eMUe6ZieBaLFckprpcq6ANQ6WvutdKjB4/ng1mu)
-- staff: staff123 ($2a$10$q.ryHZTTNEffKDxsI6OZ8.90Dkvso/.XpdPguBfgyhSPura4GKqAC)
-- customer: customer123 ($2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6)
-- shipper: shipper123 ($2a$10$hU/EEvW4syufpJENqjh9t.OYp/NrAD72PYjvTxOlB8N1WJ5qB72zy)
SET IDENTITY_INSERT dbo.users ON;
INSERT INTO dbo.users (id, created_at, updated_at, username, password, full_name, email, phone, role, is_active, store_id) VALUES
(1, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'admin', N'$2a$10$u5O0YAExfMSe/tPj5YQuHuLJTeHT00rvl/hLLMbRRDqeBu5QBcPoW', N'Quản Trị Viên Toàn Hệ Thống', N'admin@bookstorechain.com', N'0901234567', N'ADMIN', 1, NULL),
(2, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'manager', N'$2a$10$2cUgd7RJbkKPT1eMUe6ZieBaLFckprpcq6ANQ6WvutdKjB4/ng1mu', N'Trần Quản Lý (Hà Nội)', N'manager@bookstorechain.com', N'0912345678', N'MANAGER', 1, 1),
(3, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'staff', N'$2a$10$q.ryHZTTNEffKDxsI6OZ8.90Dkvso/.XpdPguBfgyhSPura4GKqAC', N'Lê Nhân Viên (Hà Nội)', N'staff@bookstorechain.com', N'0923456789', N'STAFF', 1, 1),
(4, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'staff_hcm', N'$2a$10$lEsx0/UugRJqogm2M2qIuuey8NrZFbiSdm9QJoOlb2bqqRsM.pd7u', N'Nguyễn Nhân Viên (Sài Gòn)', N'staff_hcm@bookstorechain.com', N'0934567890', N'STAFF', 1, 2),
(5, DATEADD(day, -150, SYSDATETIME()), SYSDATETIME(), N'customer', N'$2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6', N'Nguyễn Văn Đọc', N'customer@bookstorechain.com', N'0945678901', N'CUSTOMER', 1, NULL),
(6, DATEADD(day, -120, SYSDATETIME()), SYSDATETIME(), N'khachhang', N'$2a$10$EoEaKov2P3imQ3kDFYRVqeB8G6HBP/C3UPp/qaM.90qq8npLcve4C', N'Phạm Thị Yêu Sách', N'khachhang@bookstorechain.com', N'0956789012', N'CUSTOMER', 1, NULL),
(7, DATEADD(day, -90, SYSDATETIME()), SYSDATETIME(), N'manager_dn', N'$2a$10$2cUgd7RJbkKPT1eMUe6ZieBaLFckprpcq6ANQ6WvutdKjB4/ng1mu', N'Phan Quản Lý (Đà Nẵng)', N'manager_dn@bookstorechain.com', N'0935111222', N'MANAGER', 1, 3),
(8, DATEADD(day, -80, SYSDATETIME()), SYSDATETIME(), N'staff_dn', N'$2a$10$q.ryHZTTNEffKDxsI6OZ8.90Dkvso/.XpdPguBfgyhSPura4GKqAC', N'Võ Nhân Viên (Đà Nẵng)', N'staff_dn@bookstorechain.com', N'0935333444', N'STAFF', 1, 3),
(9, DATEADD(day, -60, SYSDATETIME()), SYSDATETIME(), N'staff_center', N'$2a$10$q.ryHZTTNEffKDxsI6OZ8.90Dkvso/.XpdPguBfgyhSPura4GKqAC', N'Đỗ Nhân Viên (Cần Thơ)', N'staff_center@bookstorechain.com', N'0909555666', N'STAFF', 1, 4),
(10, DATEADD(day, -65, SYSDATETIME()), SYSDATETIME(), N'hoangnam', N'$2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6', N'Hoàng Tuấn Nam', N'hoangnam@gmail.com', N'0918112233', N'CUSTOMER', 1, NULL),
(11, DATEADD(day, -40, SYSDATETIME()), SYSDATETIME(), N'lananh', N'$2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6', N'Trần Thị Lan Anh', N'lananh.tran@gmail.com', N'0977223344', N'CUSTOMER', 1, NULL),
(12, DATEADD(day, -25, SYSDATETIME()), SYSDATETIME(), N'minhduc', N'$2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6', N'Đặng Minh Đức', N'minhduc.dang@gmail.com', N'0988334455', N'CUSTOMER', 1, NULL),
(13, DATEADD(day, -10, SYSDATETIME()), SYSDATETIME(), N'thuyle', N'$2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6', N'Lê Thị Thúy', N'thuyle99@gmail.com', N'0933445566', N'CUSTOMER', 1, NULL),
(14, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), N'quanghuy', N'$2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6', N'Phạm Quang Huy', N'quanghuy.dev@gmail.com', N'0944556677', N'CUSTOMER', 1, NULL),
(15, SYSDATETIME(), SYSDATETIME(), N'ngocmai', N'$2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6', N'Bùi Ngọc Mai', N'ngocmai.bui@gmail.com', N'0966778899', N'CUSTOMER', 1, NULL),
(16, DATEADD(day, -60, SYSDATETIME()), SYSDATETIME(), N'shipper', N'$2a$10$hU/EEvW4syufpJENqjh9t.OYp/NrAD72PYjvTxOlB8N1WJ5qB72zy', N'Ngô Văn Giao (Hà Nội)', N'shipper@bookstorechain.com', N'0961112233', N'SHIPPER', 1, 1),
(17, DATEADD(day, -60, SYSDATETIME()), SYSDATETIME(), N'shipper_hcm', N'$2a$10$hU/EEvW4syufpJENqjh9t.OYp/NrAD72PYjvTxOlB8N1WJ5qB72zy', N'Đinh Văn Chuyển (Sài Gòn)', N'shipper_hcm@bookstorechain.com', N'0962223344', N'SHIPPER', 1, 2);
SET IDENTITY_INSERT dbo.users OFF;
GO

-- 3.3. Dữ liệu Đầu Sách (25 Tác Phẩm Đa Dạng Thể Loại)
SET IDENTITY_INSERT dbo.book ON;
INSERT INTO dbo.book (id, created_at, updated_at, title, author, publisher, publication_year, category, description, cover_price, cover_image, is_active) VALUES
(1, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Hoàng Tử Bé', N'Antoine de Saint-Exupéry', N'NXB Kim Đồng', 2021, N'Văn học cổ điển', N'Một tác phẩm triết lý nhẹ nhàng, sâu lắng về tình yêu, sự cô đơn và cái nhìn trong trẻo của trẻ thơ về thế giới người lớn.', 85000, N'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop', 1),
(2, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Nhà Giả Kim', N'Paulo Coelho', N'NXB Hội Nhà Văn', 2020, N'Tiểu thuyết triết lý', N'Hành trình đi tìm kho báu của chàng chăn cừu Santiago dạy chúng ta về việc lắng nghe tiếng gọi trái tim và theo đuổi Ước mơ cuộc đời.', 79000, N'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop', 1),
(3, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Tội Ác Và Trừng Phạt', N'Fyodor Dostoevsky', N'NXB Văn Học', 2019, N'Văn học kinh điển Nga', N'Kiệt tác phân tích tâm lý sâu sắc về sự giằng xé nội tâm, tội lỗi và sự cứu rỗi của chàng sinh viên nghèo Raskolnikov.', 175000, N'https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?q=80&w=600&auto=format&fit=crop', 1),
(4, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Chiến Tranh Và Hòa Bình', N'Leo Tolstoy', N'NXB Văn Học', 2018, N'Sử thi kinh điển', N'Bộ sử thi hoành tráng khắc họa toàn cảnh xã hội Nga trong cơn bão táp của cuộc chiến tranh chống Napoléon năm 1812.', 350000, N'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=600&auto=format&fit=crop', 1),
(5, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Đại Gia Gatsby', N'F. Scott Fitzgerald', N'NXB Hội Nhà Văn', 2022, N'Văn học Mỹ', N'Bản trường ca buồn về Giấc mơ Mỹ, sự phù hoa, ảo vọng và một mối tình si cuồng trong thời đại nhạc Jazz thập niên 1920.', 98000, N'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?q=80&w=600&auto=format&fit=crop', 1),
(6, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Trăm Năm Cô Đơn', N'Gabriel García Márquez', N'NXB Văn Học', 2020, N'Hiện thực huyền ảo', N'Biên niên sử về bảy thế hệ dòng họ Buendía tại ngôi làng huyền thoại Macondo, tượng trưng cho lịch sử của cả châu Mỹ Latinh.', 160000, N'https://images.unsplash.com/photo-1476275466078-4007374efbbe?q=80&w=600&auto=format&fit=crop', 1),
(7, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Sapiens: Lược Sử Loài Người', N'Yuval Noah Harari', N'NXB Tri Thức', 2021, N'Lịch sử - Triết học', N'Khảo sát toàn cảnh hành trình tiến hóa của Homo sapiens từ một sinh vật tầm thường trở thành bá chủ hành tinh.', 220000, N'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?q=80&w=600&auto=format&fit=crop', 1),
(8, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Tư Duy Nhanh Và Chậm', N'Daniel Kahneman', N'NXB Thế Giới', 2020, N'Tâm lý học kinh tế', N'Công trình đoạt giải Nobel khám phá hai hệ thống chi phối tư duy con người: phản xạ nhanh cảm tính và tính toán chậm logic.', 250000, N'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?q=80&w=600&auto=format&fit=crop', 1),
(9, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Đi Tìm Lẽ Sống', N'Viktor E. Frankl', N'NXB Tổng Hợp TP.HCM', 2021, N'Tâm lý trị liệu', N'Trải nghiệm sinh tồn phi thường trong trại tập trung Auschwitz và phương pháp trị liệu ý nghĩa giúp con người vượt qua nghịch cảnh.', 88000, N'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?q=80&w=600&auto=format&fit=crop', 1),
(10, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Dế Mèn Phiêu Lưu Ký', N'Tô Hoài', N'NXB Kim Đồng', 2022, N'Văn học Việt Nam', N'Tác phẩm văn học thiếu nhi bất hủ thấm đượm tình bạn, khát vọng tự do và tinh thần trượng nghĩa vì hòa bình.', 65000, N'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=600&auto=format&fit=crop', 1),
(11, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Số Đỏ', N'Vũ Trọng Phụng', N'NXB Văn Học', 2020, N'Hiện thực phê phán', N'Kiệt tác trào phúng sắc sảo vạch trần xã hội Âu hóa nhố nhăng, đảo lộn luân thường đạo lý thời tiền chiến.', 75000, N'https://images.unsplash.com/photo-1516979187457-637abb4f9353?q=80&w=600&auto=format&fit=crop', 1),
(12, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'Bố Già', N'Mario Puzo', N'NXB Văn Học', 2021, N'Tiểu thuyết hình sự', N'Bức tranh kinh điển về thế giới ngầm mafia Mỹ và gia tộc Corleone, chứa đựng những triết lý sâu sắc về tình thân và quyền lực.', 145000, N'https://images.unsplash.com/photo-1463320726281-696a485928c7?q=80&w=600&auto=format&fit=crop', 1),
(13, DATEADD(day, -120, SYSDATETIME()), SYSDATETIME(), N'Đắc Nhân Tâm', N'Dale Carnegie', N'NXB Tổng Hợp TP.HCM', 2021, N'Kỹ năng sống', N'Nghệ thuật thu phục lòng người, tạo dựng thiện cảm và duy trì mối quan hệ bền vững trong cuộc sống và công việc.', 86000, N'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=600&auto=format&fit=crop', 1),
(14, DATEADD(day, -110, SYSDATETIME()), SYSDATETIME(), N'Cha Giàu Cha Nghèo', N'Robert T. Kiyosaki', N'NXB Trẻ', 2020, N'Kinh tế - Tài chính', N'Cuốn sách kinh điển về tư duy tài chính, phân biệt tài sản và tiêu sản, con đường dẫn đến độc lập và tự do tài chính.', 125000, N'https://images.unsplash.com/photo-1553729459-efe14ef6055d?q=80&w=600&auto=format&fit=crop', 1),
(15, DATEADD(day, -100, SYSDATETIME()), SYSDATETIME(), N'Cây Cam Ngọt Của Tôi', N'José Mauro de Vasconcelos', N'NXB Hội Nhà Văn', 2022, N'Văn học nước ngoài', N'Câu chuyện cảm động lấy đi nước mắt của hàng triệu độc giả về cậu bé Zezé 5 tuổi thông minh và trái tim khao khát được yêu thương.', 108000, N'https://images.unsplash.com/photo-1516979187457-637abb4f9353?q=80&w=600&auto=format&fit=crop', 1),
(16, DATEADD(day, -90, SYSDATETIME()), SYSDATETIME(), N'Mắt Biếc', N'Nguyễn Nhật Ánh', N'NXB Trẻ', 2021, N'Văn học Việt Nam', N'Mối tình đơn phương da diết của Ngạn dành cho Hà Lan giữa khung cảnh làng Đo Đo yên bình thơ mộng.', 110000, N'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop', 1),
(17, DATEADD(day, -85, SYSDATETIME()), SYSDATETIME(), N'Không Gia Đình', N'Hector Malot', N'NXB Văn Học', 2019, N'Văn học kinh điển', N'Cuộc lưu lạc đầy sóng gió nhưng chan chứa nghĩa tình của cậu bé Rémi và đoàn xiếc thú của cụ Vitalis trên khắp nước Pháp.', 140000, N'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=600&auto=format&fit=crop', 1),
(18, DATEADD(day, -80, SYSDATETIME()), SYSDATETIME(), N'Clean Code - Mã Sạch', N'Robert C. Martin', N'NXB Thông Tin & Truyền Thông', 2020, N'Công nghệ thông tin', N'Cẩm nang kinh điển về kỹ nghệ phần mềm linh hoạt, các nguyên lý viết mã nguồn sáng sủa, dễ bảo trì và mở rộng.', 280000, N'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?q=80&w=600&auto=format&fit=crop', 1),
(19, DATEADD(day, -70, SYSDATETIME()), SYSDATETIME(), N'Khéo Ăn Nói Sẽ Có Được Thiên Hạ', N'Trác Nhã', N'NXB Văn Học', 2021, N'Kỹ năng giao tiếp', N'Phương pháp ứng xử khéo léo, giao tiếp duyên dáng và thuyết phục người khác trong mọi hoàn cảnh xã hội.', 95000, N'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?q=80&w=600&auto=format&fit=crop', 1),
(20, DATEADD(day, -60, SYSDATETIME()), SYSDATETIME(), N'Tuổi Trẻ Đáng Giá Bao Nhiêu?', N'Rosie Nguyễn', N'NXB Hội Nhà Văn', 2022, N'Kỹ năng sống', N'Những lời tâm sự, đúc kết thực tế giúp người trẻ định vị bản thân, rèn luyện thói quen đọc sách và khám phá thế giới.', 80000, N'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=600&auto=format&fit=crop', 1),
(21, DATEADD(day, -50, SYSDATETIME()), SYSDATETIME(), N'Lược Sử Thời Gian', N'Stephen Hawking', N'NXB Trẻ', 2021, N'Khoa học vũ trụ', N'Kiệt tác giải thích nguồn gốc vũ trụ, hố đen, thời gian và thuyết tương đối bằng ngôn từ đại chúng dễ hiểu.', 135000, N'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?q=80&w=600&auto=format&fit=crop', 1),
(22, DATEADD(day, -45, SYSDATETIME()), SYSDATETIME(), N'Người Bán Hàng Vĩ Đại Nhất Thế Giới', N'Og Mandino', N'NXB Thế Giới', 2020, N'Kinh doanh - Bán hàng', N'10 cuộn da cổ chứa đựng những triết lý chuyển hóa nội tâm, lòng kiên trì và nghệ thuật phụng sự khách hàng đỉnh cao.', 75000, N'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop', 1),
(23, DATEADD(day, -35, SYSDATETIME()), SYSDATETIME(), N'Một Lít Nước Mắt', N'Kito Aya', N'NXB Văn Học', 2022, N'Hồi ký cảm động', N'Nhật ký kiên cường chống chọi với căn bệnh thoái hóa tiểu não của cô gái 15 tuổi lan tỏa khát vọng sống mãnh liệt.', 82000, N'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?q=80&w=600&auto=format&fit=crop', 1),
(24, DATEADD(day, -30, SYSDATETIME()), SYSDATETIME(), N'Đại Việt Sử Ký Toàn Thư', N'Ngô Sĩ Liên', N'NXB Thời Đại', 2018, N'Lịch sử Việt Nam', N'Bộ chính sử vô giá ghi chép trọn vẹn lịch sử dân tộc từ thời Hồng Bàng huyền thoại đến triều đại Hậu Lê.', 320000, N'https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?q=80&w=600&auto=format&fit=crop', 1),
(25, DATEADD(day, -20, SYSDATETIME()), SYSDATETIME(), N'Kafka Bên Bờ Biển', N'Haruki Murakami', N'NXB Hội Nhà Văn', 2021, N'Hiện thực huyền ảo', N'Mê cung kỳ ảo đan xen giữa số phận chàng trai 15 tuổi bỏ nhà ra đi và một ông lão có khả năng trò chuyện cùng loài mèo.', 165000, N'https://images.unsplash.com/photo-1476275466078-4007374efbbe?q=80&w=600&auto=format&fit=crop', 1);
SET IDENTITY_INSERT dbo.book OFF;
GO

-- 3.4. Dữ liệu Bản Sao Sách Tồn Kho (41 Bản Sách Phân Bổ 4 Chi Nhánh)
SET IDENTITY_INSERT dbo.book_copy ON;
INSERT INTO dbo.book_copy (id, created_at, updated_at, copy_code, book_id, store_id, book_condition, price, status, note) VALUES
(1, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-HN-001', 1, 1, N'LIKE_NEW', 60000, N'AVAILABLE', N'Sách sưu tầm, bìa cứng, nguyên seal giấy bóng kính.'),
(2, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-HN-002', 2, 1, N'GOOD', 50000, N'AVAILABLE', N'Ruột sách trắng đẹp không tì vết, có chữ ký lưu niệm nhỏ.'),
(3, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-HN-003', 3, 1, N'FAIR', 110000, N'AVAILABLE', N'Gáy sách hơi ngả màu thời gian, chất lượng đọc hoàn hảo.'),
(4, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-HN-004', 7, 1, N'LIKE_NEW', 165000, N'AVAILABLE', N'Bản in đặc biệt bìa cứng mạ nhũ vàng tuyệt đẹp.'),
(5, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-HN-005', 8, 1, N'GOOD', 180000, N'AVAILABLE', N'Đọc tốt, trang giấy dày dặn nguyên vẹn.'),
(6, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-HN-006', 10, 1, N'LIKE_NEW', 48000, N'AVAILABLE', N'Tranh minh họa màu nước gốc của Tạ Lựu.'),
(7, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-SG-001', 1, 2, N'GOOD', 55000, N'AVAILABLE', N'Bản kỷ niệm 75 năm xuất bản.'),
(8, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-SG-002', 5, 2, N'LIKE_NEW', 75000, N'AVAILABLE', N'Bìa jacket nguyên vẹn, dịch giả chuẩn xác.'),
(9, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-SG-003', 6, 2, N'FAIR', 105000, N'AVAILABLE', N'Chữ rõ nét, giấy xốp chống lóa.'),
(10, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-SG-004', 9, 2, N'LIKE_NEW', 65000, N'AVAILABLE', N'Sách được bảo quản trong tủ kính chuyên dụng.'),
(11, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-SG-005', 11, 2, N'GOOD', 52000, N'AVAILABLE', N'Bản in ấn phẩm sưu tầm của NXB Văn Học.'),
(12, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-SG-006', 12, 2, N'LIKE_NEW', 115000, N'AVAILABLE', N'Bìa da sang trọng, giấy thơm mùi gỗ sồi.'),
(13, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-DN-001', 2, 3, N'GOOD', 45000, N'AVAILABLE', N'Trang giấy đều đẹp, thích hợp đọc nghiền ngẫm.'),
(14, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-DN-002', 4, 3, N'LIKE_NEW', 250000, N'AVAILABLE', N'Bộ 2 tập nguyên hộp bọc vải cao cấp.'),
(15, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-DN-003', 7, 3, N'GOOD', 155000, N'AVAILABLE', N'Bản dịch của dịch giả Giáp Văn Dương.'),
(16, DATEADD(day, -180, SYSDATETIME()), SYSDATETIME(), N'OB-DN-004', 10, 3, N'FAIR', 40000, N'AVAILABLE', N'Ấn bản thiếu nhi bìa mềm tiện dụng.'),
-- Các bản sách mới kích hoạt KPI Cảnh báo tồn lâu (> 90 ngày) & Cạn kho (<= 2 cuốn)
(17, DATEADD(day, -105, SYSDATETIME()), SYSDATETIME(), N'OB-HN-007', 13, 1, N'LIKE_NEW', 65000, N'AVAILABLE', N'Bản in tái bản bìa gập cao cấp (Tồn kho > 90 ngày).'),
(18, DATEADD(day, -98, SYSDATETIME()), SYSDATETIME(), N'OB-HN-008', 14, 1, N'GOOD', 85000, N'AVAILABLE', N'Ruột sách còn rất mới, giấy sáng rõ (Tồn kho > 90 ngày).'),
(19, DATEADD(day, -45, SYSDATETIME()), SYSDATETIME(), N'OB-HN-009', 18, 1, N'LIKE_NEW', 210000, N'AVAILABLE', N'Sách công nghệ IT chuyên sâu cho lập trình viên (Chỉ có 1 cuốn duy nhất - Cạn kho).'),
(20, DATEADD(day, -30, SYSDATETIME()), SYSDATETIME(), N'OB-HN-010', 20, 1, N'GOOD', 55000, N'AVAILABLE', N'Gáy thẳng thắn, không ghi chú bút mực.'),
(21, DATEADD(day, -20, SYSDATETIME()), SYSDATETIME(), N'OB-HN-011', 24, 1, N'FAIR', 190000, N'AVAILABLE', N'Trọn bộ sử học bìa cứng hơi ngả màu thời gian.'),
(22, DATEADD(day, -10, SYSDATETIME()), SYSDATETIME(), N'OB-HN-012', 15, 1, N'LIKE_NEW', 82000, N'SOLD', N'Đã bán thành công cho khách hàng.'),
(23, DATEADD(day, -115, SYSDATETIME()), SYSDATETIME(), N'OB-SG-007', 3, 2, N'POOR', 60000, N'AVAILABLE', N'Bìa sờn mép nhẹ, giấy vàng cổ điển (Tồn kho > 90 ngày).'),
(24, DATEADD(day, -50, SYSDATETIME()), SYSDATETIME(), N'OB-SG-008', 13, 2, N'GOOD', 58000, N'AVAILABLE', N'Ấn phẩm phổ thông bán chạy.'),
(25, DATEADD(day, -40, SYSDATETIME()), SYSDATETIME(), N'OB-SG-009', 16, 2, N'LIKE_NEW', 85000, N'AVAILABLE', N'Bìa áo poster phim, có chữ ký lưu niệm tác giả.'),
(26, DATEADD(day, -25, SYSDATETIME()), SYSDATETIME(), N'OB-SG-010', 19, 2, N'GOOD', 68000, N'AVAILABLE', N'Gáy sách chắc chắn, mép trang sạch sẽ.'),
(27, DATEADD(day, -15, SYSDATETIME()), SYSDATETIME(), N'OB-SG-011', 25, 2, N'LIKE_NEW', 125000, N'AVAILABLE', N'Ấn phẩm đặc biệt minh họa đẹp mắt.'),
(28, DATEADD(day, -5, SYSDATETIME()), SYSDATETIME(), N'OB-SG-012', 2, 2, N'GOOD', 52000, N'SOLD', N'Đã bán qua kênh trực tiếp tại quầy.'),
(29, DATEADD(day, -100, SYSDATETIME()), SYSDATETIME(), N'OB-DN-005', 8, 3, N'GOOD', 160000, N'AVAILABLE', N'Bản dịch công phu, giấy xốp chống cận (Tồn kho > 90 ngày).'),
(30, DATEADD(day, -60, SYSDATETIME()), SYSDATETIME(), N'OB-DN-006', 15, 3, N'LIKE_NEW', 80000, N'AVAILABLE', N'Bản in mới cứng, phù hợp sưu tầm làm quà tặng.'),
(31, DATEADD(day, -35, SYSDATETIME()), SYSDATETIME(), N'OB-DN-007', 17, 3, N'FAIR', 85000, N'AVAILABLE', N'Bìa có nếp gấp nhẹ góc dưới, nội dung nguyên vẹn.'),
(32, DATEADD(day, -18, SYSDATETIME()), SYSDATETIME(), N'OB-DN-008', 21, 3, N'GOOD', 95000, N'AVAILABLE', N'Sách thiên văn khoa học bìa gập.'),
(33, DATEADD(day, -7, SYSDATETIME()), SYSDATETIME(), N'OB-DN-009', 22, 3, N'LIKE_NEW', 58000, N'HOLDING', N'Khách hàng đặt trước chờ lấy cuối tuần.'),
(34, DATEADD(day, -45, SYSDATETIME()), SYSDATETIME(), N'OB-CT-001', 1, 4, N'LIKE_NEW', 62000, N'AVAILABLE', N'Bản in kỷ niệm Hoàng Tử Bé.'),
(35, DATEADD(day, -30, SYSDATETIME()), SYSDATETIME(), N'OB-CT-002', 2, 4, N'GOOD', 55000, N'AVAILABLE', N'Nhà Giả Kim bản dịch kinh điển.'),
(36, DATEADD(day, -20, SYSDATETIME()), SYSDATETIME(), N'OB-CT-003', 14, 4, N'LIKE_NEW', 92000, N'AVAILABLE', N'Cha Giàu Cha Nghèo tập 1.'),
(37, DATEADD(day, -10, SYSDATETIME()), SYSDATETIME(), N'OB-CT-004', 16, 4, N'GOOD', 78000, N'AVAILABLE', N'Mắt Biếc bản bìa mềm truyện dài.'),
(38, DATEADD(day, -4, SYSDATETIME()), SYSDATETIME(), N'OB-CT-005', 23, 4, N'LIKE_NEW', 65000, N'AVAILABLE', N'Một Lít Nước Mắt - Sách tâm lý cảm động.'),
(39, DATEADD(day, -4, SYSDATETIME()), SYSDATETIME(), N'OB-TRANS-001', 7, 1, N'LIKE_NEW', 160000, N'IN_TRANSIT', N'Đang trên đường luân chuyển từ Hà Nội đi Đà Nẵng.'),
(40, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), N'OB-TRANS-002', 18, 2, N'GOOD', 200000, N'IN_TRANSIT', N'Đang luân chuyển từ Sài Gòn sang Cần Thơ.'),
(41, DATEADD(day, -1, SYSDATETIME()), SYSDATETIME(), N'OB-TRANS-003', 13, 3, N'LIKE_NEW', 65000, N'IN_TRANSIT', N'Luân chuyển bổ sung từ Đà Nẵng về Hà Nội.');
SET IDENTITY_INSERT dbo.book_copy OFF;
GO

-- 3.5. Dữ liệu Thu Mua Sách Cũ (12 Đơn Hàng Phân Bổ 4 Trạng Thái & Nhiều Mốc Thời Gian)
SET IDENTITY_INSERT dbo.buyback_request ON;
INSERT INTO dbo.buyback_request (id, created_at, updated_at, user_id, store_id, status, staff_note) VALUES
(1, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 5, 1, N'PENDING', N'Khách hàng mang sách đến thẩm định vào sáng thứ Hai (Quá 24h chờ duyệt).'),
(2, DATEADD(day, -20, SYSDATETIME()), SYSDATETIME(), 6, 2, N'COMPLETED', N'Đã kiểm tra kỹ tình trạng, thanh toán chuyển khoản thành công cho khách hàng.'),
(3, DATEADD(day, -2, SYSDATETIME()), SYSDATETIME(), 10, 1, N'PENDING', N'Yêu cầu gửi từ cổng khách hàng trực tuyến (Quá 24h chờ duyệt).'),
(4, DATEADD(day, -2, SYSDATETIME()), SYSDATETIME(), 11, 2, N'PENDING', N'Khách gửi 2 đầu sách kinh tế qua cổng online, chờ quản lý chi nhánh thẩm định giá.'),
(5, DATEADD(hour, -36, SYSDATETIME()), SYSDATETIME(), 12, 3, N'PENDING', N'Khách mang trực tiếp đến quầy, nhân viên bận chưa hoàn tất quy trình nhập.'),
(6, DATEADD(hour, -2, SYSDATETIME()), SYSDATETIME(), 10, 1, N'PENDING', N'Đơn mới tiếp nhận từ khách hàng Hoàng Tuấn Nam.'),
(7, DATEADD(day, -5, SYSDATETIME()), SYSDATETIME(), 13, 1, N'APPROVED', N'Đã đồng ý định giá 150.000đ cho bộ sách. Hẹn khách mang đến chi nhánh Tràng Tiền.'),
(8, DATEADD(day, -4, SYSDATETIME()), SYSDATETIME(), 14, 2, N'APPROVED', N'Định giá bộ truyện tranh sưu tầm thành công. Khách đồng ý chốt giá.'),
(9, DATEADD(day, -6, SYSDATETIME()), SYSDATETIME(), 15, 4, N'REJECTED', N'Từ chối: Sách bị ố vàng loang lổ nặng, rách rời gáy và thiếu hơn 10 trang ruột.'),
(10, DATEADD(day, -12, SYSDATETIME()), SYSDATETIME(), 10, 1, N'COMPLETED', N'Đã thanh toán tiền mặt 230.000đ và bàn giao thủ kho in mã vạch nhập quầy.'),
(11, DATEADD(day, -18, SYSDATETIME()), SYSDATETIME(), 12, 3, N'COMPLETED', N'Chuyển khoản thành công 140.000đ cho khách hàng Đặng Minh Đức.'),
(12, DATEADD(day, -8, SYSDATETIME()), SYSDATETIME(), 11, 4, N'COMPLETED', N'Đã tiếp nhận 3 cuốn sách văn học kinh điển, thanh toán trực tiếp tại quầy 180.000đ.');
SET IDENTITY_INSERT dbo.buyback_request OFF;
GO

-- Chi tiết các đầu sách trong đơn thu mua
SET IDENTITY_INSERT dbo.buyback_item ON;
INSERT INTO dbo.buyback_item (id, created_at, updated_at, buyback_request_id, book_title, book_author, proposed_condition, offered_price, item_image) VALUES
(1, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 1, N'Suối Nguồn (The Fountainhead)', N'Ayn Rand', N'GOOD', 120000, N'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop'),
(2, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 1, N'Hạt Giống Tâm Hồn - Tập 1', N'Nhiều tác giả', N'FAIR', 25000, N'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop'),
(3, DATEADD(day, -20, SYSDATETIME()), SYSDATETIME(), 2, N'Đắc Nhân Tâm (Bản in xưa)', N'Dale Carnegie', N'LIKE_NEW', 65000, N'https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?q=80&w=600&auto=format&fit=crop'),
(4, DATEADD(day, -2, SYSDATETIME()), SYSDATETIME(), 4, N'Dạy Con Làm Giàu - Tập 2', N'Robert Kiyosaki', N'GOOD', 65000, N'https://images.unsplash.com/photo-1553729459-efe14ef6055d?q=80&w=600&auto=format&fit=crop'),
(5, DATEADD(day, -2, SYSDATETIME()), SYSDATETIME(), 4, N'Bí Mật Tư Duy Triệu Phú', N'T. Harv Eker', N'FAIR', 45000, N'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=600&auto=format&fit=crop'),
(6, DATEADD(hour, -36, SYSDATETIME()), SYSDATETIME(), 5, N'Hành Trình Về Phương Đông', N'Baird T. Spalding', N'GOOD', 55000, N'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?q=80&w=600&auto=format&fit=crop'),
(7, DATEADD(hour, -2, SYSDATETIME()), SYSDATETIME(), 6, N'Rừng Na Uy', N'Haruki Murakami', N'LIKE_NEW', 85000, N'https://images.unsplash.com/photo-1476275466078-4007374efbbe?q=80&w=600&auto=format&fit=crop'),
(8, DATEADD(day, -5, SYSDATETIME()), SYSDATETIME(), 7, N'Sherlock Holmes Toàn Tập - Tập 1', N'Arthur Conan Doyle', N'GOOD', 75000, N'https://images.unsplash.com/photo-1463320726281-696a485928c7?q=80&w=600&auto=format&fit=crop'),
(9, DATEADD(day, -5, SYSDATETIME()), SYSDATETIME(), 7, N'Sherlock Holmes Toàn Tập - Tập 2', N'Arthur Conan Doyle', N'GOOD', 75000, N'https://images.unsplash.com/photo-1463320726281-696a485928c7?q=80&w=600&auto=format&fit=crop'),
(10, DATEADD(day, -4, SYSDATETIME()), SYSDATETIME(), 8, N'Dragon Ball - 7 Viên Ngọc Rồng Trọn Bộ', N'Akira Toriyama', N'FAIR', 120000, N'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=600&auto=format&fit=crop'),
(11, DATEADD(day, -6, SYSDATETIME()), SYSDATETIME(), 9, N'Sách Lập Trình Pascal Xưa', N'Nhiều tác giả', N'POOR', 0, N'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?q=80&w=600&auto=format&fit=crop'),
(12, DATEADD(day, -12, SYSDATETIME()), SYSDATETIME(), 10, N'Muôn Kiếp Nhân Sinh - Tập 1', N'Nguyên Phong', N'LIKE_NEW', 110000, N'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop'),
(13, DATEADD(day, -12, SYSDATETIME()), SYSDATETIME(), 10, N'Muôn Kiếp Nhân Sinh - Tập 2', N'Nguyên Phong', N'LIKE_NEW', 120000, N'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop'),
(14, DATEADD(day, -18, SYSDATETIME()), SYSDATETIME(), 11, N'Hiểu Về Trái Tim', N'Thầy Minh Niệm', N'GOOD', 75000, N'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop'),
(15, DATEADD(day, -8, SYSDATETIME()), SYSDATETIME(), 12, N'Tiếng Chim Hót Trong Bụi Mận Gai', N'Colleen McCullough', N'GOOD', 90000, N'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=600&auto=format&fit=crop');
SET IDENTITY_INSERT dbo.buyback_item OFF;
GO

-- 3.6. Dữ liệu Điều Chuyển Kho (8 Phiếu Tạo Nên Ma Trận Luân Chuyển Đa Chiều Toàn Diện)
SET IDENTITY_INSERT dbo.stock_transfer ON;
INSERT INTO dbo.stock_transfer (id, created_at, updated_at, sender_store_id, receiver_store_id, creator_id, status, note) VALUES
(1, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 1, 2, 3, N'IN_TRANSIT', N'Điều chuyển bổ sung sách văn học cổ điển cho chi nhánh Sài Gòn cuối tuần (Quá 48h trễ hẹn).'),
(2, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 1, 3, 3, N'IN_TRANSIT', N'Luân chuyển gấp 1 bản sách Sapiens sang Đà Nẵng phục vụ độc giả (Quá 48h trễ hẹn).'),
(3, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 2, 4, 4, N'IN_TRANSIT', N'Điều chuyển sách chuyên ngành IT cho chi nhánh miền Tây (Quá 48h trễ hẹn).'),
(4, DATEADD(hour, -12, SYSDATETIME()), SYSDATETIME(), 3, 1, 8, N'IN_TRANSIT', N'Hỗ trợ cân bằng tồn kho Đắc Nhân Tâm về kho Tràng Tiền.'),
(5, DATEADD(day, -7, SYSDATETIME()), SYSDATETIME(), 1, 2, 3, N'COMPLETED', N'Đã bàn giao đầy đủ cho thủ kho Sài Gòn, sách nguyên vẹn tem niêm phong.'),
(6, DATEADD(day, -15, SYSDATETIME()), SYSDATETIME(), 2, 3, 4, N'COMPLETED', N'Điều chuyển sách văn học kinh điển sang chi nhánh miền Trung thành công.'),
(7, DATEADD(hour, -3, SYSDATETIME()), SYSDATETIME(), 1, 4, 3, N'PENDING', N'Phiếu đề xuất chi viện sách mới cho chi nhánh cơ sở mới khai trương.'),
(8, DATEADD(day, -5, SYSDATETIME()), SYSDATETIME(), 4, 2, 9, N'CANCELLED', N'Hủy phiếu: Chi nhánh Sài Gòn đã tự cân đối được nguồn sách tại chỗ.');
SET IDENTITY_INSERT dbo.stock_transfer OFF;
GO

-- Chi tiết bản sách điều chuyển
SET IDENTITY_INSERT dbo.stock_transfer_item ON;
INSERT INTO dbo.stock_transfer_item (id, created_at, updated_at, stock_transfer_id, book_copy_id) VALUES
(1, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 1, 3),
(2, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 2, 39),
(3, DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 3, 40),
(4, DATEADD(hour, -12, SYSDATETIME()), SYSDATETIME(), 4, 41),
(5, DATEADD(day, -7, SYSDATETIME()), SYSDATETIME(), 5, 20),
(6, DATEADD(day, -15, SYSDATETIME()), SYSDATETIME(), 6, 26),
(7, DATEADD(hour, -3, SYSDATETIME()), SYSDATETIME(), 7, 21);
SET IDENTITY_INSERT dbo.stock_transfer_item OFF;
GO

-- 3.9. Mã giảm giá mẫu: đủ các trạng thái để thử (đang chạy, theo chi nhánh, hết hạn, hết lượt, sắp diễn ra, tạm dừng)
INSERT INTO dbo.promotion (created_at, updated_at, code, description, type, value, min_order_amount, max_discount_amount, starts_at, ends_at, usage_limit, used_count, one_per_customer, store_id, is_active) VALUES
(SYSDATETIME(), SYSDATETIME(), N'WELCOME10', N'Giảm 10% cho đơn từ 100.000đ, tối đa 50.000đ', N'PERCENT', 10, 100000, 50000, DATEADD(day, -30, SYSDATETIME()), DATEADD(day, 90, SYSDATETIME()), NULL, 0, 1, NULL, 1),
(SYSDATETIME(), SYSDATETIME(), N'GIAM20K', N'Giảm 20.000đ cho đơn từ 150.000đ', N'FIXED', 20000, 150000, NULL, DATEADD(day, -10, SYSDATETIME()), DATEADD(day, 60, SYSDATETIME()), 100, 0, 1, NULL, 1),
(SYSDATETIME(), SYSDATETIME(), N'HANOI15', N'Giảm 15% cho đơn tại chi nhánh Hà Nội, tối đa 80.000đ', N'PERCENT', 15, 0, 80000, DATEADD(day, -5, SYSDATETIME()), DATEADD(day, 30, SYSDATETIME()), 50, 0, 1, 1, 1),
(SYSDATETIME(), SYSDATETIME(), N'TETXUAN', N'Khuyến mãi Tết đã kết thúc', N'PERCENT', 20, 0, NULL, DATEADD(day, -90, SYSDATETIME()), DATEADD(day, -30, SYSDATETIME()), NULL, 12, 1, NULL, 1),
(SYSDATETIME(), SYSDATETIME(), N'VIPLIMIT', N'Giảm 30.000đ cho 5 khách đầu tiên (đã hết lượt)', N'FIXED', 30000, 0, NULL, DATEADD(day, -20, SYSDATETIME()), DATEADD(day, 20, SYSDATETIME()), 5, 5, 1, NULL, 1),
(SYSDATETIME(), SYSDATETIME(), N'SAPDIENRA', N'Khuyến mãi sắp diễn ra', N'PERCENT', 25, 200000, 100000, DATEADD(day, 7, SYSDATETIME()), DATEADD(day, 37, SYSDATETIME()), NULL, 0, 1, NULL, 1),
(SYSDATETIME(), SYSDATETIME(), N'TAMDUNG', N'Mã đang tạm dừng', N'FIXED', 10000, 0, NULL, DATEADD(day, -5, SYSDATETIME()), DATEADD(day, 30, SYSDATETIME()), NULL, 0, 0, NULL, 0);
GO

-- 3.10. Đánh giá mẫu
INSERT INTO dbo.review (created_at, updated_at, user_id, book_id, rating, comment, is_hidden) VALUES
(DATEADD(day, -20, SYSDATETIME()), SYSDATETIME(), 5, 1, 5, N'Bản in đẹp, giấy ngả vàng đúng chất sách cũ. Giao nhận nhanh tại chi nhánh.', 0),
(DATEADD(day, -12, SYSDATETIME()), SYSDATETIME(), 6, 1, 4, N'Sách sạch sẽ, chỉ có vài nếp gấp nhỏ ở bìa như mô tả.', 0),
(DATEADD(day, -15, SYSDATETIME()), SYSDATETIME(), 5, 2, 5, N'Đúng tình trạng như mô tả, rất đáng tiền.', 0),
(DATEADD(day, -9, SYSDATETIME()), SYSDATETIME(), 6, 2, 3, N'Nội dung hay nhưng bìa hơi cũ hơn mình tưởng.', 0),
(DATEADD(day, -7, SYSDATETIME()), SYSDATETIME(), 5, 3, 4, NULL, 0),
(DATEADD(day, -3, SYSDATETIME()), SYSDATETIME(), 6, 4, 1, N'Nội dung quảng cáo lừa đảo, đừng mua! Liên hệ ngay website xyz để nhận ưu đãi.', 1);
GO

-- 3.11. Sách yêu thích mẫu
-- in_stock_notified = 1 vì các sách này đang có hàng (chỉ báo "có hàng lại" khi sách hết rồi có lại)
INSERT INTO dbo.wishlist_item (created_at, updated_at, user_id, book_id, in_stock_notified) VALUES
(DATEADD(day, -5, SYSDATETIME()), SYSDATETIME(), 5, 1, 1),
(DATEADD(day, -2, SYSDATETIME()), SYSDATETIME(), 5, 4, 1),
(DATEADD(day, -1, SYSDATETIME()), SYSDATETIME(), 6, 2, 1);
GO

-- 3.12. Thông báo riêng mẫu cho khách hàng 'customer'
INSERT INTO dbo.user_notification (created_at, updated_at, user_id, type, title, message, link, is_read) VALUES
(DATEADD(hour, -2, SYSDATETIME()), SYSDATETIME(), 5, 'WISHLIST', N'Sách yêu thích đã có hàng', N'"Chiến Tranh Và Hòa Bình" vừa có hàng trở lại. Đặt sớm kẻo hết.', '/books/4', 0),
(DATEADD(day, -1, SYSDATETIME()), SYSDATETIME(), 5, 'ORDER', N'Đơn hàng sẵn sàng nhận', N'Đơn hàng của bạn đã sẵn sàng, mời bạn đến chi nhánh nhận sách.', '/orders', 0),
(DATEADD(day, -4, SYSDATETIME()), SYSDATETIME(), 5, 'BUYBACK', N'Yêu cầu thu mua đã được duyệt', N'Nhân viên đã định giá sách bạn gửi. Mời bạn mang sách đến chi nhánh.', NULL, 1);
GO

-- 3.13. Chat hỗ trợ mẫu
INSERT INTO dbo.chat_conversation (created_at, updated_at, customer_id, store_id, status, last_message_at, last_message_preview, customer_unread, staff_unread) VALUES
(DATEADD(hour, -3, SYSDATETIME()), SYSDATETIME(), 5, 1, 'OPEN', DATEADD(minute, -40, SYSDATETIME()), N'Dạ bản này còn, bạn ghé chi nhánh trước 20h nhé.', 1, 0),
(DATEADD(minute, -25, SYSDATETIME()), SYSDATETIME(), 6, 2, 'OPEN', DATEADD(minute, -25, SYSDATETIME()), N'Chi nhánh mình có nhận đổi sách cũ lấy sách mới không ạ?', 0, 1);
GO
INSERT INTO dbo.chat_message (created_at, updated_at, conversation_id, sender_id, from_customer, content) VALUES
(DATEADD(hour, -3, SYSDATETIME()), SYSDATETIME(), 1, 5, 1, N'Cho mình hỏi cuốn "Hoàng Tử Bé" bản bìa cứng còn không ạ?'),
(DATEADD(minute, -40, SYSDATETIME()), SYSDATETIME(), 1, 3, 0, N'Dạ bản này còn, bạn ghé chi nhánh trước 20h nhé.'),
(DATEADD(minute, -25, SYSDATETIME()), SYSDATETIME(), 2, 6, 1, N'Chi nhánh mình có nhận đổi sách cũ lấy sách mới không ạ?');
GO
INSERT INTO dbo.user_notification (created_at, updated_at, user_id, type, title, message, link, is_read) VALUES
(DATEADD(minute, -40, SYSDATETIME()), SYSDATETIME(), 5, 'CHAT', N'Tin nhắn mới từ OldBook Hà Nội — Tràng Tiền', N'Dạ bản này còn, bạn ghé chi nhánh trước 20h nhé.', '/chat?conversation=1', 0);
GO

-- 3.14. Lịch sử bán hàng online mẫu (để dashboard và báo cáo doanh thu có dữ liệu khi trình diễn)
-- 24 đơn đã hoàn tất rải trong 30 ngày qua ở 4 chi nhánh, cộng 3 đơn đang xử lý. Mỗi đơn bán một bản sách riêng
-- (tạo thêm bản sao OB-SOLD-xx: SOLD cho đơn đã bán, RESERVED cho đơn còn đang xử lý) nên không đụng tới tồn kho mẫu.
DECLARE @i INT = 1;
WHILE @i <= 27
BEGIN
    DECLARE @open BIT = CASE WHEN @i > 24 THEN 1 ELSE 0 END;
    DECLARE @storeId BIGINT = (@i % 4) + 1;
    DECLARE @customerId BIGINT = CHOOSE((@i % 8) + 1, 5, 6, 10, 11, 12, 13, 14, 15);
    DECLARE @bookId BIGINT = ((@i * 5) % 7) + 1;
    DECLARE @price NUMERIC(12,2) = 45000 + ((@i * 37) % 9) * 18000;
    DECLARE @delivery BIT = CASE WHEN @i % 3 = 0 THEN 1 ELSE 0 END;
    DECLARE @bank BIT = CASE WHEN @i % 2 = 0 THEN 1 ELSE 0 END;
    DECLARE @discount NUMERIC(12,2) = CASE WHEN @i % 5 = 0 THEN ROUND(@price * 0.10, 0) ELSE 0 END;
    DECLARE @ship NUMERIC(12,2) = CASE WHEN @delivery = 1 THEN 25000 ELSE 0 END;
    DECLARE @total NUMERIC(12,2) = @price - @discount + @ship;
    DECLARE @daysAgo INT = (@i * 7) % 30;
    DECLARE @placed DATETIME2(6);
    DECLARE @done DATETIME2(6);
    DECLARE @status NVARCHAR(20);

    IF @open = 1
    BEGIN
        SET @placed = CASE @i WHEN 25 THEN DATEADD(MINUTE, -10, SYSDATETIME()) WHEN 26 THEN DATEADD(HOUR, -3, SYSDATETIME()) ELSE DATEADD(DAY, -1, SYSDATETIME()) END;
        SET @done = NULL;
        SET @status = CASE @i WHEN 25 THEN N'PENDING' WHEN 26 THEN N'CONFIRMED' ELSE N'READY_FOR_PICKUP' END;
    END
    ELSE
    BEGIN
        -- Các đơn i = 8, 16, 24 hoàn tất trong hôm nay (vài chục phút trước); còn lại hoàn tất từ 9h đến 18h các ngày trước
        IF @i % 8 = 0
            SET @done = DATEADD(MINUTE, -(@i * 3), SYSDATETIME());
        ELSE
            SET @done = DATEADD(HOUR, 9 + (@i % 10), CAST(CAST(DATEADD(DAY, -CASE WHEN @daysAgo = 0 THEN 1 ELSE @daysAgo END, SYSDATETIME()) AS DATE) AS DATETIME2(6)));
        SET @placed = DATEADD(HOUR, -20, @done);
        SET @status = N'COMPLETED';
    END

    DECLARE @title NVARCHAR(200) = (SELECT title FROM dbo.book WHERE id = @bookId);
    DECLARE @copyCode NVARCHAR(50) = N'OB-SOLD-' + RIGHT('00' + CAST(@i AS VARCHAR(3)), 2);
    DECLARE @receiver NVARCHAR(100) = (SELECT full_name FROM dbo.users WHERE id = @customerId);
    DECLARE @orderCode NVARCHAR(30) = N'OB-' + CONVERT(CHAR(8), ISNULL(@done, @placed), 112) + N'-S' + RIGHT('00' + CAST(@i AS VARCHAR(3)), 2);

    INSERT INTO dbo.book_copy (created_at, updated_at, copy_code, book_id, store_id, book_condition, price, status, note)
    VALUES (@placed, SYSDATETIME(), @copyCode, @bookId, @storeId, N'GOOD', @price, CASE WHEN @open = 1 THEN N'RESERVED' ELSE N'SOLD' END, NULL);
    DECLARE @copyId BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.customer_order (created_at, updated_at, order_code, user_id, store_id, status, fulfillment_type, receiver_name, receiver_phone,
                                    shipping_address, subtotal, discount_amount, shipping_fee, total_amount, completed_at, promotion_code)
    VALUES (@placed, ISNULL(@done, @placed), @orderCode, @customerId, @storeId, @status, CASE WHEN @delivery = 1 THEN N'DELIVERY' ELSE N'PICKUP' END,
            @receiver, N'0900000' + RIGHT('000' + CAST(@i AS VARCHAR(3)), 3),
            CASE WHEN @delivery = 1 THEN N'12 Phố Huế, Hai Bà Trưng, Hà Nội' ELSE NULL END,
            @price, @discount, @ship, @total, @done, CASE WHEN @discount > 0 THEN N'WELCOME10' ELSE NULL END);
    DECLARE @orderId BIGINT = SCOPE_IDENTITY();

    INSERT INTO dbo.order_item (created_at, updated_at, order_id, book_copy_id, price_snapshot, title_snapshot, copy_code_snapshot)
    VALUES (@placed, @placed, @orderId, @copyId, @price, @title, @copyCode);

    INSERT INTO dbo.payment (created_at, updated_at, order_id, method, status, amount, transaction_code, paid_at)
    VALUES (@placed, ISNULL(@done, @placed), @orderId, CASE WHEN @bank = 1 THEN N'BANK_TRANSFER' ELSE N'COD' END,
            CASE WHEN @status = N'COMPLETED' OR (@bank = 1 AND @status <> N'PENDING') THEN N'PAID' ELSE N'UNPAID' END, @total,
            CASE WHEN @bank = 1 AND (@status = N'COMPLETED' OR @status <> N'PENDING') THEN N'FT2610' + RIGHT('0000' + CAST(@i AS VARCHAR(4)), 4) ELSE NULL END,
            CASE WHEN @status = N'COMPLETED' OR (@bank = 1 AND @status <> N'PENDING') THEN ISNULL(@done, @placed) ELSE NULL END);

    SET @i += 1;
END
GO

PRINT N'>>> HOÀN TẤT KHỞI TẠO CƠ SỞ DỮ LIỆU BOOKSTORE CHAIN ĐA DẠNG THÀNH CÔNG! <<<';
GO
