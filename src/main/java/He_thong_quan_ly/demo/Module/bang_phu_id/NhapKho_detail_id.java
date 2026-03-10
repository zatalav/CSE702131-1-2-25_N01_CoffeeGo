package He_thong_quan_ly.demo.Module.bang_phu_id;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class NhapKho_detail_id implements Serializable {
    @Column(name = "Nhapkho_id")
    private String Nhapkho_id;

    @Column(name = "nguyenlieu_id")
    private String nguyenlieu_id;

    public NhapKho_detail_id(String nhapkhoId, String nguyenlieuId) {
        this.Nhapkho_id = nhapkhoId;
        this.nguyenlieu_id = nguyenlieuId;
    }

    public void setNhapkho_id(String nhapkhoId) {
        this.Nhapkho_id = nhapkhoId;
    }

    public void setNguyenlieu_id(String nguyenlieuId) {
        this.nguyenlieu_id = nguyenlieuId;
    }

    public String getNhapkhoId() {
        return Nhapkho_id;
    }

    public void setNhapkhoId(String nhapkhoId) {
        this.Nhapkho_id = nhapkhoId;
    }

    public String getNguyenlieuId() {
        return nguyenlieu_id;
    }

    public void setNguyenlieuId(String nguyenlieuId) {
        this.nguyenlieu_id = nguyenlieuId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NhapKho_detail_id that)) {
            return false;
        }
        return Objects.equals(Nhapkho_id, that.Nhapkho_id)
                && Objects.equals(nguyenlieu_id, that.nguyenlieu_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Nhapkho_id, nguyenlieu_id);
    }
}
