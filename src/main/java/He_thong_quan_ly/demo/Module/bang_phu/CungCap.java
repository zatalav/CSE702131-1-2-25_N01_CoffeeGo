package He_thong_quan_ly.demo.Module.bang_phu;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.Admin.NhaCungCap_module;
import He_thong_quan_ly.demo.Module.bang_phu_id.CungCapId;
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
@Table(name = "CungCap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CungCap {

    @EmbeddedId
    private CungCapId id;

    @ManyToOne
    @MapsId("nguyenLieuId")
    @JoinColumn(name = "nguyenlieu_id")
    private NguyenLieu_module nguyenLieu;

    @ManyToOne
    @MapsId("nhaCungCapId")
    @JoinColumn(name = "nhacungcap_id")
    private NhaCungCap_module nhaCungCap;

    public CungCapId getId() {
        return id;
    }

    public void setId(CungCapId id) {
        this.id = id;
    }

    public NguyenLieu_module getNguyenLieu() {
        return nguyenLieu;
    }

    public void setNguyenLieu(NguyenLieu_module nguyenLieu) {
        this.nguyenLieu = nguyenLieu;
    }

    public NhaCungCap_module getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(NhaCungCap_module nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }
}
