package He_thong_quan_ly.demo.Module.bang_phu_id;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class giohang_detail_id implements Serializable {
    @Column(name = "gio_hang_id")
    private String GioHang_id;

    @Column(name = "sanpham_id")
    private String sanpham_id;

    @Column(name = "size")
    private String size;

    public giohang_detail_id(String gioHang_id, String sanpham_id, String size) {
        this.GioHang_id = gioHang_id;
        this.sanpham_id = sanpham_id;
        this.size = size;
    }

    public String getGioHang_id() {
        return GioHang_id;
    }

    public void setGioHang_id(String gioHang_id) {
        this.GioHang_id = gioHang_id;
    }

    public String getSanpham_id() {
        return sanpham_id;
    }

    public void setSanpham_id(String sanpham_id) {
        this.sanpham_id = sanpham_id;
    }

    public String getGioHangId() {
        return GioHang_id;
    }

    public void setGioHangId(String gioHangId) {
        this.GioHang_id = gioHangId;
    }

    public String getSanphamId() {
        return sanpham_id;
    }

    public void setSanphamId(String sanphamId) {
        this.sanpham_id = sanphamId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof giohang_detail_id that)) {
            return false;
        }
        return Objects.equals(GioHang_id, that.GioHang_id)
                && Objects.equals(sanpham_id, that.sanpham_id)
                && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GioHang_id, sanpham_id, size);
    }

}
