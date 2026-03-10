package He_thong_quan_ly.demo.Module.Admin;

import java.util.List;

import He_thong_quan_ly.demo.Module.bang_phu.CungCap;
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
@Table(name = "NhaCungCap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhaCungCap_module {

    @Id
    @Column(name = "nhacungcap_id")
    private String nhacungcapId;

    @Column(name = "ten_nha_cung_cap")
    private String tenNhaCungCap;

    @Column(name = "sdt")
    private String sdt;

    @Column(name = "email")
    private String email;

    @Column(name = "dia_chi")
    private String diaChi;

    @OneToMany(mappedBy = "nhaCungCap")
    private List<CungCap> dsCungCap;

    public String getNhacungcapId() {
        return nhacungcapId;
    }

    public void setNhacungcapId(String nhacungcapId) {
        this.nhacungcapId = nhacungcapId;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public List<CungCap> getDsCungCap() {
        return dsCungCap;
    }

    public void setDsCungCap(List<CungCap> dsCungCap) {
        this.dsCungCap = dsCungCap;
    }
}
