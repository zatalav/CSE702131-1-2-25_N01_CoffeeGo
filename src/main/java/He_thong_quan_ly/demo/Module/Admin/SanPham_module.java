package He_thong_quan_ly.demo.Module.Admin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import He_thong_quan_ly.demo.Module.bang_phu.CongThuc;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "san_pham", indexes = {
        @Index(name = "idx_sanpham_danhmuc", columnList = "danhmuc_id"),
        @Index(name = "idx_sanpham_trangthai", columnList = "trang_thai"),
        @Index(name = "idx_sanpham_tensp", columnList = "ten_sp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPham_module {

    @Id
    @Column(name = "sanpham_id", length = 20)
    private String sanPhamId;

    @ManyToOne
    @JoinColumn(name = "danhmuc_id", nullable = false)
    private DanhMuc_module danhMuc;

    @Column(name = "ten_sp", nullable = false)
    private String tenSp;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "gia")
    private Long gia;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CongThuc> congThucs;

    // Keep explicit accessor for tooling that does not expand Lombok symbols.
    public String getSanPhamId() {
        return sanPhamId;
    }

    public void setSanPhamId(String sanPhamId) {
        this.sanPhamId = sanPhamId;
    }
}
