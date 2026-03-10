package He_thong_quan_ly.demo.Module.NhanVienKho;

import java.time.LocalDate;

import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nhap_kho", indexes = {
        @Index(name = "idx_nhapkho_ngaynhap", columnList = "ngay_nhap"),
        @Index(name = "idx_nhapkho_nhanvien", columnList = "nhanvien_id")
})
public class NhapKho_module {
    @Id
    private String nhapkhoId;

    @ManyToOne
    @JoinColumn(name = "nhanvien_id")
    private NhanVien_module nhanVien;

    @Column(name = "ngay_nhap")
    private LocalDate ngayNhap;

    @Column(name = "Tongtien")
    private long tongTien;

    private String ghiChu;

    // Backward-compatible accessors for legacy call sites.
    public String getNhapkho_id() {
        return nhapkhoId;
    }

    public void setNhapkho_id(String nhapkhoId) {
        this.nhapkhoId = nhapkhoId;
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

    public LocalDate getNgay_nhap() {
        return ngayNhap;
    }

    public void setNgay_nhap(LocalDate ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
