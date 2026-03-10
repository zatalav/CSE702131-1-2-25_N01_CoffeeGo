package He_thong_quan_ly.demo.Module.Admin;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "KhachHang", indexes = {
        @Index(name = "idx_khachhang_gmail", columnList = "gmail"),
        @Index(name = "idx_khachhang_sdt", columnList = "sdt"),
        @Index(name = "idx_khachhang_login_password", columnList = "gmail,password")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHang_module {
    @Id
    @Column(name = "khachhang_id")
    private String khachhang_id;

    @Column(name = "ten_kh")
    private String Ten_KH;

    @Column(name = "gmail")
    private String Gmail;

    @Column(name = "sdt")
    private String SDT;

    @Column(name = "password")
    private String Password;

    @Column(name = "gioi_tinh")
    private String Gioi_tinh;

    @Column(name = "ngay_sinh")
    private LocalDate Ngay_sinh;

    @Column(name = "dia_chi")
    private String Dia_chi;

    @Column(name = "tong_so_dh_mua")
    private Integer Tong_so_DH_mua;

    @Column(name = "trang_thai")
    private String Trang_thai;

    @Column(name = "img_kh")
    private String imgKh;

    public String getKhachhang_id() {
        return khachhang_id;
    }

    public void setKhachhang_id(String khachhang_id) {
        this.khachhang_id = khachhang_id;
    }

    public String getTen_KH() {
        return Ten_KH;
    }

    public void setTen_KH(String ten_KH) {
        Ten_KH = ten_KH;
    }

    public String getGmail() {
        return Gmail;
    }

    public void setGmail(String gmail) {
        Gmail = gmail;
    }

    public String getSDT() {
        return SDT;
    }

    public void setSDT(String sDT) {
        SDT = sDT;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getGioi_tinh() {
        return Gioi_tinh;
    }

    public void setGioi_tinh(String gioi_tinh) {
        Gioi_tinh = gioi_tinh;
    }

    public LocalDate getNgay_sinh() {
        return Ngay_sinh;
    }

    public void setNgay_sinh(LocalDate ngay_sinh) {
        Ngay_sinh = ngay_sinh;
    }

    public String getDia_chi() {
        return Dia_chi;
    }

    public void setDia_chi(String dia_chi) {
        Dia_chi = dia_chi;
    }

    public Integer getTong_so_DH_mua() {
        return Tong_so_DH_mua;
    }

    public void setTong_so_DH_mua(Integer tong_so_DH_mua) {
        Tong_so_DH_mua = tong_so_DH_mua;
    }

    public String getTrang_thai() {
        return Trang_thai;
    }

    public void setTrang_thai(String trang_thai) {
        Trang_thai = trang_thai;
    }

    public String getImgKh() {
        return imgKh;
    }

    public void setImgKh(String imgKh) {
        this.imgKh = imgKh;
    }
}
