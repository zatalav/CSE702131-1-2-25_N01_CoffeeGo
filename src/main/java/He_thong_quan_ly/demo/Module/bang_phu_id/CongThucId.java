package He_thong_quan_ly.demo.Module.bang_phu_id;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CongThucId implements Serializable {

    private String nguyenLieuId;
    private String sanPhamId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CongThucId that)) {
            return false;
        }
        return Objects.equals(nguyenLieuId, that.nguyenLieuId)
                && Objects.equals(sanPhamId, that.sanPhamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nguyenLieuId, sanPhamId);
    }
}
