package He_thong_quan_ly.demo.Module.NhanVienKho;

import He_thong_quan_ly.demo.Module.Admin.CoSo_module;
import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kho_bep", indexes = {
        @Index(name = "idx_khobep_nguyenlieu", columnList = "nguyenlieu_id"),
        @Index(name = "idx_khobep_xuatkho", columnList = "xuatkho_id")
})
@IdClass(KhobepId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Khobep_module {

    @Id
    @ManyToOne
    @JoinColumn(name = "coso_id")
    private CoSo_module coSo;

    @Id
    @Column(name = "nguyenlieu_id")
    private String nguyenlieuId;

    @ManyToOne
    @JoinColumn(name = "nguyenlieu_id", insertable = false, updatable = false)
    private NguyenLieu_module nguyenLieu;

    @ManyToOne
    @JoinColumn(name = "xuatkho_id")
    private XuatKho_module xuatKho;

    @Column(name = "sl_ton")
    private double slTon;

    @Column(name = "don_vi")
    private String donVi;

    // Keep explicit accessors for IDE analyzers that cannot resolve
    // Lombok-generated methods.
    public CoSo_module getCoSo() {
        return coSo;
    }

    public void setCoSo(CoSo_module coSo) {
        this.coSo = coSo;
    }

    public String getNguyenlieuId() {
        return nguyenlieuId;
    }

    public void setNguyenlieuId(String nguyenlieuId) {
        this.nguyenlieuId = nguyenlieuId;
    }

    public NguyenLieu_module getNguyenLieu() {
        return nguyenLieu;
    }

    public void setNguyenLieu(NguyenLieu_module nguyenLieu) {
        this.nguyenLieu = nguyenLieu;
    }

    public double getSlTon() {
        return slTon;
    }

    public void setSlTon(double slTon) {
        this.slTon = slTon;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }
}
