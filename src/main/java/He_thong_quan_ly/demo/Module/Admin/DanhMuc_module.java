package He_thong_quan_ly.demo.Module.Admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DanhMuc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DanhMuc_module {

    @Id
    @Column(name = "danhmuc_id")
    private String danhmucId;

    @Column(name = "ten_dm")
    private String tenDm;

    public String getDanhmucId() {
        return danhmucId;
    }

    public void setDanhmucId(String danhmucId) {
        this.danhmucId = danhmucId;
    }

    public String getTenDm() {
        return tenDm;
    }

    public void setTenDm(String tenDm) {
        this.tenDm = tenDm;
    }
}
