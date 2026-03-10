-- Performance index migration for high-frequency lookup and sort queries.
-- Run once on MySQL before production deployment.

-- DonHang (order) hot paths
CREATE INDEX IF NOT EXISTS idx_donhang_nhanvien_ngaydat ON DonHang (nhanvien_id, ngay_dat);
CREATE INDEX IF NOT EXISTS idx_donhang_khachhang_ngaydat ON DonHang (khachhang_id, ngay_dat);
CREATE INDEX IF NOT EXISTS idx_donhang_status_loai_ngaydat ON DonHang (trang_thai, phan_loai, ngay_dat);
CREATE INDEX IF NOT EXISTS idx_donhang_magiamgia ON DonHang (magiamgia_id);
CREATE INDEX IF NOT EXISTS idx_donhang_ngaydat ON DonHang (ngay_dat);

-- NhanVien / KhachHang auth and directory lookups
CREATE INDEX IF NOT EXISTS idx_nhanvien_gmail ON NhanVien (gmail);
CREATE INDEX IF NOT EXISTS idx_nhanvien_coso ON NhanVien (coso_id);
CREATE INDEX IF NOT EXISTS idx_nhanvien_sdt ON NhanVien (sdt);

CREATE INDEX IF NOT EXISTS idx_khachhang_gmail ON KhachHang (gmail);
CREATE INDEX IF NOT EXISTS idx_khachhang_sdt ON KhachHang (sdt);
CREATE INDEX IF NOT EXISTS idx_khachhang_login_password ON KhachHang (gmail, password);

-- Product catalog and variants
CREATE INDEX IF NOT EXISTS idx_sanpham_danhmuc ON san_pham (danhmuc_id);
CREATE INDEX IF NOT EXISTS idx_sanpham_trangthai ON san_pham (trang_thai);
CREATE INDEX IF NOT EXISTS idx_sanpham_tensp ON san_pham (ten_sp);

CREATE INDEX IF NOT EXISTS idx_spvariant_sanpham ON san_pham_variant (sanpham_id);
CREATE INDEX IF NOT EXISTS idx_spvariant_sanpham_size ON san_pham_variant (sanpham_id, size);
CREATE INDEX IF NOT EXISTS idx_spvariant_sanpham_price ON san_pham_variant (sanpham_id, price);

-- Cart
CREATE INDEX IF NOT EXISTS idx_giohang_khachhang ON gio_hang (khachhang_id);
CREATE INDEX IF NOT EXISTS idx_giohangdetail_sanpham ON gio_hang_detail (sanpham_id);

-- Warehouse import/export
CREATE INDEX IF NOT EXISTS idx_nhapkho_ngaynhap ON nhap_kho (ngay_nhap);
CREATE INDEX IF NOT EXISTS idx_nhapkho_nhanvien ON nhap_kho (nhanvien_id);

CREATE INDEX IF NOT EXISTS idx_xuatkho_nhanvien ON xuat_kho (nhanvien_id);
CREATE INDEX IF NOT EXISTS idx_xuatkho_coso ON xuat_kho (coso_id);
CREATE INDEX IF NOT EXISTS idx_xuatkho_ngayxuat ON xuat_kho (ngay_xuat);

CREATE INDEX IF NOT EXISTS idx_xuatkhoct_xuatkho ON xuat_kho_chi_tiet (xuatkho_id);
CREATE INDEX IF NOT EXISTS idx_xuatkhoct_nguyenlieu ON xuat_kho_chi_tiet (nguyenlieu_id);
CREATE INDEX IF NOT EXISTS idx_xuatkhoct_coso ON xuat_kho_chi_tiet (coso_id);

CREATE INDEX IF NOT EXISTS idx_khobep_nguyenlieu ON kho_bep (nguyenlieu_id);
CREATE INDEX IF NOT EXISTS idx_khobep_xuatkho ON kho_bep (xuatkho_id);

-- Formula / supply / order details
CREATE INDEX IF NOT EXISTS idx_congthuc_sanpham ON CongThuc (sanpham_id);
CREATE INDEX IF NOT EXISTS idx_congthuc_nguyenlieu ON CongThuc (nguyenlieu_id);
CREATE INDEX IF NOT EXISTS idx_cungcap_ncc_nguyenlieu ON CungCap (nhacungcap_id, nguyenlieu_id);
CREATE INDEX IF NOT EXISTS idx_nguyenlieu_ten ON NguyenLieu (Ten_nguyen_lieu);
CREATE INDEX IF NOT EXISTS idx_donhangdetail_sanpham ON DonHang_detail (sanpham_id);
CREATE INDEX IF NOT EXISTS idx_donhangdetail_donhang ON DonHang_detail (donhang_id);
