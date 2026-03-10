package He_thong_quan_ly.demo.Module.bang_phu;

import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.bang_phu_id.giohang_detail_id;
import He_thong_quan_ly.demo.Module.customer.giohang_module;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
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
@Table(name = "gio_hang_detail")
@NoArgsConstructor
@AllArgsConstructor
public class giohang_detail {
    @AttributeOverrides({
            @AttributeOverride(name = "GioHang_id", column = @Column(name = "gio_hang_id")),
            @AttributeOverride(name = "sanpham_id", column = @Column(name = "sanpham_id")),
            @AttributeOverride(name = "size", column = @Column(name = "size"))
    })
    @EmbeddedId
    private giohang_detail_id id;

    @ManyToOne
    @MapsId("sanpham_id")
    @JoinColumn(name = "sanpham_id", referencedColumnName = "sanpham_id")
    private SanPham_module sanpham;

    @ManyToOne
    @MapsId("GioHang_id")
    @JoinColumn(name = "gio_hang_id", referencedColumnName = "gio_hang_id")
    private giohang_module giohang;

    @Column(name = "so_luong")
    private int soLuong;

    @Column(name = "size", insertable = false, updatable = false)
    private String size;

    @Column(name = "sugar")
    private String sugar;

    @Column(name = "ice")
    private String ice;

    @Column(name = "milk")
    private String milk;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    public giohang_detail_id getId() {
        return id;
    }

    public void setId(giohang_detail_id id) {
        this.id = id;
    }

    public SanPham_module getSanpham() {
        return sanpham;
    }

    public void setSanpham(SanPham_module sanpham) {
        this.sanpham = sanpham;
    }

    public giohang_module getGiohang() {
        return giohang;
    }

    public void setGiohang(giohang_module giohang) {
        this.giohang = giohang;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSugar() {
        return sugar;
    }

    public void setSugar(String sugar) {
        this.sugar = sugar;
    }

    public String getIce() {
        return ice;
    }

    public void setIce(String ice) {
        this.ice = ice;
    }

    public String getMilk() {
        return milk;
    }

    public void setMilk(String milk) {
        this.milk = milk;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
