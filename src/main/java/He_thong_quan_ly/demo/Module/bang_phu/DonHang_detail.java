package He_thong_quan_ly.demo.Module.bang_phu;

import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.bang_phu_id.donhang_detail_id;
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
@Table(name = "DonHang_detail")
@NoArgsConstructor
@AllArgsConstructor
public class DonHang_detail {
    @AttributeOverrides({
            @AttributeOverride(name = "donhang_id", column = @Column(name = "donhang_id")),
            @AttributeOverride(name = "sanpham_id", column = @Column(name = "sanpham_id")),
            @AttributeOverride(name = "size", column = @Column(name = "size"))
    })
    @EmbeddedId
    private donhang_detail_id id;

    @ManyToOne
    @MapsId("sanpham_id")
    @JoinColumn(name = "sanpham_id")
    private SanPham_module sanPham;

    @ManyToOne
    @MapsId("donhang_id")
    @JoinColumn(name = "donhang_id")
    private DonHang_module donhang;

    private int SL;

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

    public donhang_detail_id getId() {
        return id;
    }

    public void setId(donhang_detail_id id) {
        this.id = id;
    }

    public SanPham_module getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham_module sanPham) {
        this.sanPham = sanPham;
    }

    public DonHang_module getDonhang() {
        return donhang;
    }

    public void setDonhang(DonHang_module donhang) {
        this.donhang = donhang;
    }

    public int getSL() {
        return SL;
    }

    public void setSL(int sL) {
        SL = sL;
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
