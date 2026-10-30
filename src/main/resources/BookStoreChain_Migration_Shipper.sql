-- ===================================================================================
-- MIGRATION: THÊM VAI TRÒ SHIPPER VÀ HAI TRẠNG THÁI ĐƠN GIAO HÀNG
-- Dùng cho CSDL BookStoreChain đã có dữ liệu (không xoá bảng, không mất dữ liệu).
-- CSDL tạo mới từ BookStoreChain_Init.sql đã có sẵn các thay đổi này, không cần chạy file này.
--
-- Việc cần làm: nới hai ràng buộc CHECK (users.role, customer_order.status) để nhận
--   role    SHIPPER
--   status  READY_FOR_DELIVERY, DELIVERY_FAILED
-- Hai cột customer_order.shipper_id và delivery_failure_reason do Hibernate (ddl-auto: update)
-- tự thêm khi khởi động ứng dụng; hai tài khoản shipper mẫu do DataInitializer tạo.
-- Chạy được nhiều lần: mỗi lần xoá ràng buộc cũ rồi tạo lại.
-- Chạy bằng: sqlcmd -S localhost -U <user> -P <password> -C -f 65001 -i BookStoreChain_Migration_Shipper.sql
-- ===================================================================================

USE BookStoreChain;
GO

DECLARE @sql NVARCHAR(MAX) = N'';

SELECT @sql += N'ALTER TABLE dbo.users DROP CONSTRAINT ' + QUOTENAME(name) + N'; '
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID(N'dbo.users') AND CHARINDEX(N'[role]', definition) > 0;

SELECT @sql += N'ALTER TABLE dbo.customer_order DROP CONSTRAINT ' + QUOTENAME(name) + N'; '
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID(N'dbo.customer_order') AND CHARINDEX(N'[status]', definition) > 0;

EXEC sp_executesql @sql;
GO

ALTER TABLE dbo.users ADD CONSTRAINT CK_users_role
    CHECK (role IN (N'CUSTOMER', N'STAFF', N'MANAGER', N'ADMIN', N'SHIPPER'));
GO

ALTER TABLE dbo.customer_order ADD CONSTRAINT CK_customer_order_status
    CHECK (status IN (N'PENDING', N'CONFIRMED', N'READY_FOR_PICKUP', N'READY_FOR_DELIVERY', N'SHIPPING', N'DELIVERY_FAILED', N'COMPLETED', N'CANCELLED'));
GO
