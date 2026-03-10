package He_thong_quan_ly.demo.Module.bang_phu;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.bang_phu_id.CongThucId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "CongThuc")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CongThucId.class)
public class CongThuc {

    @Id
    @Column(name = "nguyenlieu_id")
    private String nguyenLieuId;

    @Id
    @Column(name = "sanpham_id")
    private String sanPhamId;

    @ManyToOne
    @JoinColumn(name = "nguyenlieu_id", insertable = false, updatable = false)
    private NguyenLieu_module nguyenLieu;

    @ManyToOne
    @JoinColumn(name = "sanpham_id", insertable = false, updatable = false)
    private SanPham_module sanPham;

    @Column(name = "SL")
    private Double soLuong;

    @Column(name = "Don_vi")
    private String donVi;

    public String getNguyenLieuId() {
        return nguyenLieuId;
    }

    public void setNguyenLieuId(String nguyenLieuId) {
        this.nguyenLieuId = nguyenLieuId;
    }

    public String getSanPhamId() {
        return sanPhamId;
    }

    public void setSanPhamId(String sanPhamId) {
        this.sanPhamId = sanPhamId;
    }

    public NguyenLieu_module getNguyenLieu() {
        return nguyenLieu;
    }

    public void setNguyenLieu(NguyenLieu_module nguyenLieu) {
        this.nguyenLieu = nguyenLieu;
    }

    public SanPham_module getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham_module sanPham) {
        this.sanPham = sanPham;
    }

    public Double getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Double soLuong) {
        this.soLuong = soLuong;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }
}
