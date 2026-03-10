package He_thong_quan_ly.demo.Module.bang_phu;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.Admin.NhaCungCap_module;
import He_thong_quan_ly.demo.Module.NhanVienKho.NhapKho_module;
import He_thong_quan_ly.demo.Module.bang_phu_id.NhapKho_detail_id;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "NhapKho_detail")
@NoArgsConstructor
@AllArgsConstructor
public class NhapKho_detail {
    @EmbeddedId
    private NhapKho_detail_id id;

    @ManyToOne
    @MapsId("Nhapkho_id")
    @JoinColumn(name = "Nhapkho_id")
    private NhapKho_module nhapkho;

    @ManyToOne
    @MapsId("nguyenlieu_id")
    @JoinColumn(name = "nguyenlieu_id")
    private NguyenLieu_module nguyenlieu;

    @ManyToOne
    @JoinColumn(name = "nhacungcap_id")
    private NhaCungCap_module nhaCungCap;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "don_vi")
    private String donVi;

    public NguyenLieu_module getNguyenlieu() {
        return nguyenlieu;
    }

    public NhapKho_detail_id getId() {
        return id;
    }

    public void setId(NhapKho_detail_id id) {
        this.id = id;
    }

    public NhapKho_module getNhapkho() {
        return nhapkho;
    }

    public void setNhapkho(NhapKho_module nhapkho) {
        this.nhapkho = nhapkho;
    }

    public void setNguyenlieu(NguyenLieu_module nguyenlieu) {
        this.nguyenlieu = nguyenlieu;
    }

    public NhaCungCap_module getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(NhaCungCap_module nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

}
