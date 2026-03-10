package He_thong_quan_ly.demo.Module.NhanVienKho;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "xuat_kho_chi_tiet", indexes = {
        @Index(name = "idx_xuatkhoct_xuatkho", columnList = "xuatkho_id"),
        @Index(name = "idx_xuatkhoct_nguyenlieu", columnList = "nguyenlieu_id"),
        @Index(name = "idx_xuatkhoct_coso", columnList = "coso_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class XuatKhoChiTiet {
    @EmbeddedId
    private XuatKhoChiTietId id;

    @ManyToOne
    @MapsId("xuatkho_id")
    @JoinColumn(name = "xuatkho_id")
    private XuatKho_module xuatKho;

    @ManyToOne
    @MapsId("nguyenlieu_id")
    @JoinColumn(name = "nguyenlieu_id")
    private NguyenLieu_module nguyenLieu;

    @Column(name = "so_luong")
    private int soLuong;

    @Column(name = "coso_id")
    private String cosoId;

    public XuatKhoChiTietId getId() {
        return id;
    }

    public XuatKho_module getXuatKho() {
        return xuatKho;
    }

    public NguyenLieu_module getNguyenLieu() {
        return nguyenLieu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public String getCosoId() {
        return cosoId;
    }
}
