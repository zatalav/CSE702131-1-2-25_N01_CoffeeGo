package He_thong_quan_ly.demo.Module.NhanVienKho;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class XuatKhoChiTietId implements Serializable {
    private String xuatkho_id;
    private String nguyenlieu_id;

    public String getXuatkhoId() {
        return xuatkho_id;
    }

    public void setXuatkhoId(String xuatkhoId) {
        this.xuatkho_id = xuatkhoId;
    }

    public String getNguyenlieuId() {
        return nguyenlieu_id;
    }

    public void setNguyenlieuId(String nguyenlieuId) {
        this.nguyenlieu_id = nguyenlieuId;
    }
}
