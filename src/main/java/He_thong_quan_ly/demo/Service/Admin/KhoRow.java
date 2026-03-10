package He_thong_quan_ly.demo.Service.Admin;

public class KhoRow {
    private String maPhieu;
    private String loaiPhieu;
    private String tenNhanVien;
    private String tenNguyenLieu;
    private Integer soLuong;
    private String donVi;
    private String ghiChu;

    public KhoRow() {
    }

    public KhoRow(String maPhieu, String loaiPhieu, String tenNhanVien, String tenNguyenLieu, Integer soLuong,
            String donVi, String ghiChu) {
        this.maPhieu = maPhieu;
        this.loaiPhieu = loaiPhieu;
        this.tenNhanVien = tenNhanVien;
        this.tenNguyenLieu = tenNguyenLieu;
        this.soLuong = soLuong;
        this.donVi = donVi;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieu() {
        return maPhieu;
    }

    public String getLoaiPhieu() {
        return loaiPhieu;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public String getTenNguyenLieu() {
        return tenNguyenLieu;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public String getDonVi() {
        return donVi;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setMaPhieu(String maPhieu) {
        this.maPhieu = maPhieu;
    }

    public void setLoaiPhieu(String loaiPhieu) {
        this.loaiPhieu = loaiPhieu;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public void setTenNguyenLieu(String tenNguyenLieu) {
        this.tenNguyenLieu = tenNguyenLieu;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
