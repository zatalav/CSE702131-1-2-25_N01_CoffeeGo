package He_thong_quan_ly.demo.Module.Admin;

import java.time.LocalDateTime;

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
@Table(name = "DonHang", indexes = {
        @Index(name = "idx_donhang_nhanvien_ngaydat", columnList = "nhanvien_id,ngay_dat"),
        @Index(name = "idx_donhang_khachhang_ngaydat", columnList = "khachhang_id,ngay_dat"),
        @Index(name = "idx_donhang_status_loai_ngaydat", columnList = "trang_thai,phan_loai,ngay_dat"),
        @Index(name = "idx_donhang_magiamgia", columnList = "magiamgia_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonHang_module {

    @Id
    private String donhang_id;

    @ManyToOne
    @JoinColumn(name = "khachhang_id")
    private KhachHang_module khachHang;

    @ManyToOne
    @JoinColumn(name = "nhanvien_id")
    private NhanVien_module nhanVien;

    @ManyToOne
    @JoinColumn(name = "magiamgia_id")
    private MaGiamGia_module maGiamGia;

    @Column(name = "tong_tien")
    private Long Tong_tien;

    @Column(name = "ngay_dat")
    private LocalDateTime Ngay_dat;

    @Column(name = "trang_thai")
    private String Trang_thai;

    @Column(name = "ly_do")
    private String Ly_do;

    @Column(name = "phan_loai")
    private String Phan_loai;

    @Column(name = "ghi_chu")
    private String Ghi_chu;

    @Column(name = "phuong_thuc_thanh_toan")
    private String paymentMethod;

    @Column(name = "trang_thai_thanh_toan")
    private String paymentStatus;

    @Column(name = "phi_ship")
    private Long Phi_ship;

    @Column(name = "khoang_cach_km")
    private Double Khoang_cach_km;

    @Column(name = "thuong_giao_hang")
    private Long Thuong_giao_hang;

    @Column(name = "dia_chi_giao")
    private String Dia_chi_giao;
}
