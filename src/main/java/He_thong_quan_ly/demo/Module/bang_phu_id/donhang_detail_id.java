package He_thong_quan_ly.demo.Module.bang_phu_id;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class donhang_detail_id implements Serializable {
    private String donhang_id;
    private String sanpham_id;
    private String size;

    public donhang_detail_id(String donhang_id, String sanpham_id, String size) {
        this.donhang_id = donhang_id;
        this.sanpham_id = sanpham_id;
        this.size = size;
    }

    public String getDonhangId() {
        return donhang_id;
    }

    public void setDonhangId(String donhangId) {
        this.donhang_id = donhangId;
    }

    public String getSanphamId() {
        return sanpham_id;
    }

    public void setSanphamId(String sanphamId) {
        this.sanpham_id = sanphamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof donhang_detail_id that)) {
            return false;
        }
        return Objects.equals(donhang_id, that.donhang_id)
                && Objects.equals(sanpham_id, that.sanpham_id)
                && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donhang_id, sanpham_id, size);
    }
}
