package He_thong_quan_ly.demo.Module.Admin;

import java.time.LocalDate;
import java.util.List;

import He_thong_quan_ly.demo.Module.bang_phu.CungCap;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "NguyenLieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NguyenLieu_module {

    @Id
    @Column(name = "nguyenlieu_id")
    private String nguyenlieuId;

    @Column(name = "Ten_nguyen_lieu")
    private String tenNguyenLieu;

    @Column(name = "Don_vi")
    private String donVi;

    @Column(name = "SL_ton")
    private int slTon;

    @Column(name = "Gia_nhap")
    private Long giaNhap;

    @Column(name = "Trang_thai")
    private String trangThai;

    @Column(name = "Han_su_dung")
    private LocalDate hanSuDung;

    @OneToMany(mappedBy = "nguyenLieu")
    private List<CungCap> dsCungCap;

    @Transient
    private String tenNhaCungCap;

}
