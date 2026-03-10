package He_thong_quan_ly.demo.Module.Admin;

import java.time.LocalDate;

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
@Table(name = "NhanVien", indexes = {
        @Index(name = "idx_nhanvien_gmail", columnList = "gmail"),
        @Index(name = "idx_nhanvien_coso", columnList = "coso_id"),
        @Index(name = "idx_nhanvien_sdt", columnList = "sdt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhanVien_module {

    @Id
    @Column(name = "nhanvien_id")
    private String nhanvienId;

    @Column(name = "ten_nv")
    private String tenNv;

    @Column(name = "chuc_vu")
    private String chucVu;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "sdt")
    private String sdt;

    @Column(name = "gmail")
    private String gmail;

    @Column(name = "password")
    private String password;

    @Column(name = "img_nv")
    private String imgNv;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @ManyToOne
    @JoinColumn(name = "coso_id")
    private CoSo_module coSo;

    public String getNhanvienId() {
        return nhanvienId;
    }

    public void setNhanvienId(String nhanvienId) {
        this.nhanvienId = nhanvienId;
    }

    public String getTenNv() {
        return tenNv;
    }

    public void setTenNv(String tenNv) {
        this.tenNv = tenNv;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImgNv() {
        return imgNv;
    }

    public void setImgNv(String imgNv) {
        this.imgNv = imgNv;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public CoSo_module getCoSo() {
        return coSo;
    }

    public void setCoSo(CoSo_module coSo) {
        this.coSo = coSo;
    }
}
