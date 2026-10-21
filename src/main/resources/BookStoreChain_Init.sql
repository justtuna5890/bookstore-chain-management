-- ===================================================================================
-- SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU & DỮ LIỆU MẪU DỰ ÁN BOOKSTORE CHAIN (OLDBOOK)
-- HỆ QUẢN TRỊ CSDL: MICROSOFT SQL SERVER
-- DATABASE: BookStoreChain
-- BẢNG MÃ: Unicode (NVARCHAR, Collation: Vietnamese_CI_AS)
-- TỰ ĐỘNG SINH BỞI JPA HIBERNATE & HỖ TRỢ CHẠY ĐỘC LẬP TRÊN SSMS / SQLCMD
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
    role NVARCHAR(20) NOT NULL CHECK (role IN (N'CUSTOMER', N'STAFF', N'MANAGER', N'ADMIN')),
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
    status NVARCHAR(20) NOT NULL CHECK (status IN (N'AVAILABLE', N'SOLD', N'IN_TRANSIT', N'HOLDING')),
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

-- 3. CHÈN DỮ LIỆU MẪU ĐẦY ĐỦ CÁC VAI TRÒ & CHỨC NĂNG

-- 3.1. Dữ liệu Chi Nhánh
SET IDENTITY_INSERT dbo.store ON;
INSERT INTO dbo.store (id, created_at, updated_at, name, address, phone, is_active) VALUES
(1, SYSDATETIME(), SYSDATETIME(), N'OldBook Hà Nội — Tràng Tiền', N'Số 12 Tràng Tiền, Quận Hoàn Kiếm, Hà Nội', N'02439123456', 1),
(2, SYSDATETIME(), SYSDATETIME(), N'OldBook Sài Gòn — Nguyễn Thị Minh Khai', N'Số 88 Nguyễn Thị Minh Khai, Quận 1, TP.HCM', N'02838123456', 1),
(3, SYSDATETIME(), SYSDATETIME(), N'OldBook Đà Nẵng — Bạch Đằng', N'Số 45 Bạch Đằng, Quận Hải Châu, Đà Nẵng', N'02363123456', 1);
SET IDENTITY_INSERT dbo.store OFF;
GO

-- 3.2. Dữ liệu Người Dùng (Đủ 4 Roles, Mật khẩu mã hóa BCrypt)
-- admin: admin123
-- manager: manager123
-- staff / staff_hcm: staff123
-- customer / khachhang: customer123
SET IDENTITY_INSERT dbo.users ON;
INSERT INTO dbo.users (id, created_at, updated_at, username, password, full_name, email, phone, role, is_active, store_id) VALUES
(1, SYSDATETIME(), SYSDATETIME(), N'admin', N'$2a$10$u5O0YAExfMSe/tPj5YQuHuLJTeHT00rvl/hLLMbRRDqeBu5QBcPoW', N'Quản Trị Viên Toàn Hệ Thống', N'admin@bookstorechain.com', N'0901234567', N'ADMIN', 1, NULL),
(2, SYSDATETIME(), SYSDATETIME(), N'manager', N'$2a$10$2cUgd7RJbkKPT1eMUe6ZieBaLFckprpcq6ANQ6WvutdKjB4/ng1mu', N'Trần Quản Lý (Hà Nội)', N'manager@bookstorechain.com', N'0912345678', N'MANAGER', 1, 1),
(3, SYSDATETIME(), SYSDATETIME(), N'staff', N'$2a$10$q.ryHZTTNEffKDxsI6OZ8.90Dkvso/.XpdPguBfgyhSPura4GKqAC', N'Lê Nhân Viên (Hà Nội)', N'staff@bookstorechain.com', N'0923456789', N'STAFF', 1, 1),
(4, SYSDATETIME(), SYSDATETIME(), N'staff_hcm', N'$2a$10$lEsx0/UugRJqogm2M2qIuuey8NrZFbiSdm9QJoOlb2bqqRsM.pd7u', N'Nguyễn Nhân Viên (Sài Gòn)', N'staff_hcm@bookstorechain.com', N'0934567890', N'STAFF', 1, 2),
(5, SYSDATETIME(), SYSDATETIME(), N'customer', N'$2a$10$s8ytujkU4N4dq7/Cv5oyOO.x0JK1cQgWsJGJwA0caystVkZArWhn6', N'Nguyễn Văn Đọc', N'customer@bookstorechain.com', N'0945678901', N'CUSTOMER', 1, NULL),
(6, SYSDATETIME(), SYSDATETIME(), N'khachhang', N'$2a$10$EoEaKov2P3imQ3kDFYRVqeB8G6HBP/C3UPp/qaM.90qq8npLcve4C', N'Phạm Thị Yêu Sách', N'khachhang@bookstorechain.com', N'0956789012', N'CUSTOMER', 1, NULL);
SET IDENTITY_INSERT dbo.users OFF;
GO

-- 3.3. Dữ liệu Đầu Sách (12 Tác Phẩm Kinh Điển)
SET IDENTITY_INSERT dbo.book ON;
INSERT INTO dbo.book (id, created_at, updated_at, title, author, publisher, publication_year, category, description, cover_price, cover_image, is_active) VALUES
(1, SYSDATETIME(), SYSDATETIME(), N'Hoàng Tử Bé', N'Antoine de Saint-Exupéry', N'NXB Kim Đồng', 2021, N'Văn học cổ điển', N'Một tác phẩm triết lý nhẹ nhàng, sâu lắng về tình yêu, sự cô đơn và cái nhìn trong trẻo của trẻ thơ về thế giới người lớn.', 85000, N'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop', 1),
(2, SYSDATETIME(), SYSDATETIME(), N'Nhà Giả Kim', N'Paulo Coelho', N'NXB Hội Nhà Văn', 2020, N'Tiểu thuyết triết lý', N'Hành trình đi tìm kho báu của chàng chăn cừu Santiago dạy chúng ta về việc lắng nghe tiếng gọi trái tim và theo đuổi Ước mơ cuộc đời.', 79000, N'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop', 1),
(3, SYSDATETIME(), SYSDATETIME(), N'Tội Ác Và Trừng Phạt', N'Fyodor Dostoevsky', N'NXB Văn Học', 2019, N'Văn học kinh điển Nga', N'Kiệt tác phân tích tâm lý sâu sắc về sự giằng xé nội tâm, tội lỗi và sự cứu rỗi của chàng sinh viên nghèo Raskolnikov.', 175000, N'https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?q=80&w=600&auto=format&fit=crop', 1),
(4, SYSDATETIME(), SYSDATETIME(), N'Chiến Tranh Và Hòa Bình', N'Leo Tolstoy', N'NXB Văn Học', 2018, N'Sử thi kinh điển', N'Bộ sử thi hoành tráng khắc họa toàn cảnh xã hội Nga trong cơn bão táp của cuộc chiến tranh chống Napoléon năm 1812.', 350000, N'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=600&auto=format&fit=crop', 1),
(5, SYSDATETIME(), SYSDATETIME(), N'Đại Gia Gatsby', N'F. Scott Fitzgerald', N'NXB Hội Nhà Văn', 2022, N'Văn học Mỹ', N'Bản trường ca buồn về Giấc mơ Mỹ, sự phù hoa, ảo vọng và một mối tình si cuồng trong thời đại nhạc Jazz thập niên 1920.', 98000, N'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?q=80&w=600&auto=format&fit=crop', 1),
(6, SYSDATETIME(), SYSDATETIME(), N'Trăm Năm Cô Đơn', N'Gabriel García Márquez', N'NXB Văn Học', 2020, N'Hiện thực huyền ảo', N'Biên niên sử về bảy thế hệ dòng họ Buendía tại ngôi làng huyền thoại Macondo, tượng trưng cho lịch sử của cả châu Mỹ Latinh.', 160000, N'https://images.unsplash.com/photo-1476275466078-4007374efbbe?q=80&w=600&auto=format&fit=crop', 1),
(7, SYSDATETIME(), SYSDATETIME(), N'Sapiens: Lược Sử Loài Người', N'Yuval Noah Harari', N'NXB Tri Thức', 2021, N'Lịch sử - Triết học', N'Khảo sát toàn cảnh hành trình tiến hóa của Homo sapiens từ một sinh vật tầm thường trở thành bá chủ hành tinh.', 220000, N'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?q=80&w=600&auto=format&fit=crop', 1),
(8, SYSDATETIME(), SYSDATETIME(), N'Tư Duy Nhanh Và Chậm', N'Daniel Kahneman', N'NXB Thế Giới', 2020, N'Tâm lý học kinh tế', N'Công trình đoạt giải Nobel khám phá hai hệ thống chi phối tư duy con người: phản xạ nhanh cảm tính và tính toán chậm logic.', 250000, N'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?q=80&w=600&auto=format&fit=crop', 1),
(9, SYSDATETIME(), SYSDATETIME(), N'Đi Tìm Lẽ Sống', N'Viktor E. Frankl', N'NXB Tổng Hợp TP.HCM', 2021, N'Tâm lý trị liệu', N'Trải nghiệm sinh tồn phi thường trong trại tập trung Auschwitz và phương pháp trị liệu ý nghĩa giúp con người vượt qua nghịch cảnh.', 88000, N'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?q=80&w=600&auto=format&fit=crop', 1),
(10, SYSDATETIME(), SYSDATETIME(), N'Dế Mèn Phiêu Lưu Ký', N'Tô Hoài', N'NXB Kim Đồng', 2022, N'Văn học Việt Nam', N'Tác phẩm văn học thiếu nhi bất hủ thấm đượm tình bạn, khát vọng tự do và tinh thần trượng nghĩa vì hòa bình.', 65000, N'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=600&auto=format&fit=crop', 1),
(11, SYSDATETIME(), SYSDATETIME(), N'Số Đỏ', N'Vũ Trọng Phụng', N'NXB Văn Học', 2020, N'Hiện thực phê phán', N'Kiệt tác trào phúng sắc sảo vạch trần xã hội Âu hóa nhố nhăng, đảo lộn luân thường đạo lý thời tiền chiến.', 75000, N'https://images.unsplash.com/photo-1516979187457-637abb4f9353?q=80&w=600&auto=format&fit=crop', 1),
(12, SYSDATETIME(), SYSDATETIME(), N'Bố Già', N'Mario Puzo', N'NXB Văn Học', 2021, N'Tiểu thuyết hình sự', N'Bức tranh kinh điển về thế giới ngầm mafia Mỹ và gia tộc Corleone, chứa đựng những triết lý sâu sắc về tình thân và quyền lực.', 145000, N'https://images.unsplash.com/photo-1463320726281-696a485928c7?q=80&w=600&auto=format&fit=crop', 1);
SET IDENTITY_INSERT dbo.book OFF;
GO

-- 3.4. Dữ liệu Bản Sao Sách Tồn Kho (16 Cuốn phân bổ 3 chi nhánh)
SET IDENTITY_INSERT dbo.book_copy ON;
INSERT INTO dbo.book_copy (id, created_at, updated_at, copy_code, book_id, store_id, book_condition, price, status, note) VALUES
(1, SYSDATETIME(), SYSDATETIME(), N'OB-HN-001', 1, 1, N'LIKE_NEW', 60000, N'AVAILABLE', N'Sách sưu tầm, bìa cứng, nguyên seal giấy bóng kính.'),
(2, SYSDATETIME(), SYSDATETIME(), N'OB-HN-002', 2, 1, N'GOOD', 50000, N'AVAILABLE', N'Ruột sách trắng đẹp không tì vết, có chữ ký lưu niệm nhỏ.'),
(3, SYSDATETIME(), SYSDATETIME(), N'OB-HN-003', 3, 1, N'FAIR', 110000, N'AVAILABLE', N'Gáy sách hơi ngả màu thời gian, chất lượng đọc hoàn hảo.'),
(4, SYSDATETIME(), SYSDATETIME(), N'OB-HN-004', 7, 1, N'LIKE_NEW', 165000, N'AVAILABLE', N'Bản in đặc biệt bìa cứng mạ nhũ vàng tuyệt đẹp.'),
(5, SYSDATETIME(), SYSDATETIME(), N'OB-HN-005', 8, 1, N'GOOD', 180000, N'AVAILABLE', N'Đọc tốt, trang giấy dày dặn nguyên vẹn.'),
(6, SYSDATETIME(), SYSDATETIME(), N'OB-HN-006', 10, 1, N'LIKE_NEW', 48000, N'AVAILABLE', N'Tranh minh họa màu nước gốc của Tạ Lựu.'),
(7, SYSDATETIME(), SYSDATETIME(), N'OB-SG-001', 1, 2, N'GOOD', 55000, N'AVAILABLE', N'Bản kỷ niệm 75 năm xuất bản.'),
(8, SYSDATETIME(), SYSDATETIME(), N'OB-SG-002', 5, 2, N'LIKE_NEW', 75000, N'AVAILABLE', N'Bìa jacket nguyên vẹn, dịch giả chuẩn xác.'),
(9, SYSDATETIME(), SYSDATETIME(), N'OB-SG-003', 6, 2, N'FAIR', 105000, N'AVAILABLE', N'Chữ rõ nét, giấy xốp chống lóa.'),
(10, SYSDATETIME(), SYSDATETIME(), N'OB-SG-004', 9, 2, N'LIKE_NEW', 65000, N'AVAILABLE', N'Sách được bảo quản trong tủ kính chuyên dụng.'),
(11, SYSDATETIME(), SYSDATETIME(), N'OB-SG-005', 11, 2, N'GOOD', 52000, N'AVAILABLE', N'Bản in ấn phẩm sưu tầm của NXB Văn Học.'),
(12, SYSDATETIME(), SYSDATETIME(), N'OB-SG-006', 12, 2, N'LIKE_NEW', 115000, N'AVAILABLE', N'Bìa da sang trọng, giấy thơm mùi gỗ sồi.'),
(13, SYSDATETIME(), SYSDATETIME(), N'OB-DN-001', 2, 3, N'GOOD', 45000, N'AVAILABLE', N'Trang giấy đều đẹp, thích hợp đọc nghiền ngẫm.'),
(14, SYSDATETIME(), SYSDATETIME(), N'OB-DN-002', 4, 3, N'LIKE_NEW', 250000, N'AVAILABLE', N'Bộ 2 tập nguyên hộp bọc vải cao cấp.'),
(15, SYSDATETIME(), SYSDATETIME(), N'OB-DN-003', 7, 3, N'GOOD', 155000, N'AVAILABLE', N'Bản dịch của dịch giả Giáp Văn Dương.'),
(16, SYSDATETIME(), SYSDATETIME(), N'OB-DN-004', 10, 3, N'FAIR', 40000, N'AVAILABLE', N'Ấn bản thiếu nhi bìa mềm tiện dụng.');
SET IDENTITY_INSERT dbo.book_copy OFF;
GO

-- 3.5. Dữ liệu Thu Mua Sách Cũ (2 Đơn mẫu: PENDING & COMPLETED)
SET IDENTITY_INSERT dbo.buyback_request ON;
INSERT INTO dbo.buyback_request (id, created_at, updated_at, user_id, store_id, status, staff_note) VALUES
(1, SYSDATETIME(), SYSDATETIME(), 5, 1, N'PENDING', N'Khách hàng mang sách đến thẩm định vào sáng thứ Hai.'),
(2, SYSDATETIME(), SYSDATETIME(), 6, 2, N'COMPLETED', N'Đã kiểm tra kỹ tình trạng, thanh toán chuyển khoản thành công cho khách hàng.');
SET IDENTITY_INSERT dbo.buyback_request OFF;
GO

SET IDENTITY_INSERT dbo.buyback_item ON;
INSERT INTO dbo.buyback_item (id, created_at, updated_at, buyback_request_id, book_title, book_author, proposed_condition, offered_price, item_image) VALUES
(1, SYSDATETIME(), SYSDATETIME(), 1, N'Suối Nguồn (The Fountainhead)', N'Ayn Rand', N'GOOD', 120000, N'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop'),
(2, SYSDATETIME(), SYSDATETIME(), 1, N'Hạt Giống Tâm Hồn - Tập 1', N'Nhiều tác giả', N'FAIR', 25000, N'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop'),
(3, SYSDATETIME(), SYSDATETIME(), 2, N'Đắc Nhân Tâm (Bản in xưa)', N'Dale Carnegie', N'LIKE_NEW', 65000, N'https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?q=80&w=600&auto=format&fit=crop');
SET IDENTITY_INSERT dbo.buyback_item OFF;
GO

-- 3.6. Dữ liệu Điều Chuyển Kho (1 Phiếu IN_TRANSIT từ Hà Nội sang Sài Gòn)
SET IDENTITY_INSERT dbo.stock_transfer ON;
INSERT INTO dbo.stock_transfer (id, created_at, updated_at, sender_store_id, receiver_store_id, creator_id, status, note) VALUES
(1, SYSDATETIME(), SYSDATETIME(), 1, 2, 3, N'IN_TRANSIT', N'Điều chuyển bổ sung sách văn học cổ điển cho chi nhánh Sài Gòn cuối tuần.');
SET IDENTITY_INSERT dbo.stock_transfer OFF;
GO

SET IDENTITY_INSERT dbo.stock_transfer_item ON;
INSERT INTO dbo.stock_transfer_item (id, created_at, updated_at, stock_transfer_id, book_copy_id) VALUES
(1, SYSDATETIME(), SYSDATETIME(), 1, 3);
SET IDENTITY_INSERT dbo.stock_transfer_item OFF;
GO

PRINT N'>>> HOÀN TẤT KHỞI TẠO CƠ SỞ DỮ LIỆU BOOKSTORE CHAIN THÀNH CÔNG! <<<';
GO
