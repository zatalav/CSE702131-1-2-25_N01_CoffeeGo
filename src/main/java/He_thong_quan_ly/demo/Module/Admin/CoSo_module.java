
package He_thong_quan_ly.demo.Module.Admin;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CoSo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoSo_module {

    @Id
    @Column(name = "coso_id")
    private String cosoId;

    @Column(name = "ten_cs")
    private String tenCs;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "SDT")
    private String sdt;

    @OneToMany(mappedBy = "coSo")
    private List<NhanVien_module> dsNhanVien;

    public String getCosoId() {
        return cosoId;
    }

    public void setCosoId(String cosoId) {
        this.cosoId = cosoId;
    }

    public String getTenCs() {
        return tenCs;
    }

    public void setTenCs(String tenCs) {
        this.tenCs = tenCs;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public List<NhanVien_module> getDsNhanVien() {
        return dsNhanVien;
    }

    public void setDsNhanVien(List<NhanVien_module> dsNhanVien) {
        this.dsNhanVien = dsNhanVien;
    }
}
