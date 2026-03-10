package He_thong_quan_ly.demo.Module.NhanVienKho;

import java.time.LocalDate;

import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "xuat_kho", indexes = {
        @Index(name = "idx_xuatkho_nhanvien", columnList = "nhanvien_id"),
        @Index(name = "idx_xuatkho_coso", columnList = "coso_id"),
        @Index(name = "idx_xuatkho_ngayxuat", columnList = "ngay_xuat")
})
public class XuatKho_module {
    @Id
    @Column(name = "xuatkho_id")
    private String xuatkhoId;

    @ManyToOne
    @JoinColumn(name = "nhanvien_id", referencedColumnName = "nhanvien_id")
    private NhanVien_module nhanVien;

    @Column(name = "coso_id")
    private String cosoId;

    @Column(name = "ngay_xuat")
    private LocalDate ngayXuat;

    @Column(name = "ghi_chu")
    private String ghiChu;

    // Backward-compatible accessors for legacy call sites.
    public String getXuatkho_id() {
        return xuatkhoId;
    }

    public void setXuatkho_id(String xuatkhoId) {
        this.xuatkhoId = xuatkhoId;
    }

    public NhanVien_module getNhanvien() {
        return nhanVien;
    }

    public void setNhanvien(NhanVien_module nhanVien) {
        this.nhanVien = nhanVien;
    }

    public NhanVien_module getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien_module nhanVien) {
        this.nhanVien = nhanVien;
    }

    public LocalDate getNgay_xuat() {
        return ngayXuat;
    }

    public void setNgay_xuat(LocalDate ngayXuat) {
        this.ngayXuat = ngayXuat;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
