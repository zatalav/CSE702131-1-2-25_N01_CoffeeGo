package He_thong_quan_ly.demo.Module.bang_phu;

import He_thong_quan_ly.demo.Module.Admin.CoSo_module;
import He_thong_quan_ly.demo.Module.NhanVienKho.XuatKho_module;
import He_thong_quan_ly.demo.Module.bang_phu_id.XuatKho_CoSo_id;
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
@Table(name = "XuatKho_CoSo")
@NoArgsConstructor
@AllArgsConstructor
public class XuatKho_CoSo {
    @EmbeddedId
    private XuatKho_CoSo_id id;

    @ManyToOne
    @MapsId("xuatkho_id")
    @JoinColumn(name = "xuatkho_id")
    private XuatKho_module xuatkho;

    @ManyToOne
    @MapsId("coso_id")
    @JoinColumn(name = "coso_id")
    private CoSo_module coSo;

    public XuatKho_CoSo_id getId() {
        return id;
    }

    public void setId(XuatKho_CoSo_id id) {
        this.id = id;
    }

    public XuatKho_module getXuatkho() {
        return xuatkho;
    }

    public void setXuatkho(XuatKho_module xuatkho) {
        this.xuatkho = xuatkho;
    }

    public CoSo_module getCoSo() {
        return coSo;
    }

    public void setCoSo(CoSo_module coSo) {
        this.coSo = coSo;
    }
}
