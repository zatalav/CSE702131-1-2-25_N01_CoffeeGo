-- Migration: hỗ trợ cùng 1 sản phẩm nhưng khác size trong giỏ hàng và đơn hàng
-- Chạy thủ công 1 lần trên MySQL trước khi chạy app nếu DB hiện tại đã có dữ liệu.

-- 1) Chuẩn hóa dữ liệu size
UPDATE gio_hang_detail
SET size = 'M'
WHERE size IS NULL OR TRIM(size) = '';

UPDATE DonHang_detail
SET size = 'M'
WHERE size IS NULL OR TRIM(size) = '';

-- 2) Đảm bảo cột size không null
ALTER TABLE gio_hang_detail
MODIFY COLUMN size VARCHAR(20) NOT NULL;

ALTER TABLE DonHang_detail
MODIFY COLUMN size VARCHAR(20) NOT NULL;

-- 3) Đổi khóa chính để bao gồm size
ALTER TABLE gio_hang_detail
DROP PRIMARY KEY,
ADD PRIMARY KEY (gio_hang_id, sanpham_id, size);

ALTER TABLE DonHang_detail
DROP PRIMARY KEY,
ADD PRIMARY KEY (donhang_id, sanpham_id, size);
