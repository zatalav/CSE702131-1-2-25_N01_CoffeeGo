package He_thong_quan_ly.demo.Module.Admin;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MaGiamGia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaGiamGia_module {

    @Id
    private String magiamgia_id;

    private String Ten_ma_gg;
    private String Mo_ta;

    // Ví dụ: "10%" hoặc "50000"
    private String giam_gia;

    private LocalDate ngay_het_han;

    private long giaTriDonToiThieu;

    private long giaTriGiamToiDa;

    // HOAT_DONG | HET_HAN
    private String Trang_thai;

    public String getMagiamgia_id() {
        return magiamgia_id;
    }

    public void setMagiamgia_id(String magiamgia_id) {
        this.magiamgia_id = magiamgia_id;
    }

    public String getTen_ma_gg() {
        return Ten_ma_gg;
    }

    public void setTen_ma_gg(String ten_ma_gg) {
        Ten_ma_gg = ten_ma_gg;
    }

    public String getMo_ta() {
        return Mo_ta;
    }

    public void setMo_ta(String mo_ta) {
        Mo_ta = mo_ta;
    }

    public String getGiam_gia() {
        return giam_gia;
    }

    public void setGiam_gia(String giam_gia) {
        this.giam_gia = giam_gia;
    }

    public LocalDate getNgay_het_han() {
        return ngay_het_han;
    }

    public void setNgay_het_han(LocalDate ngay_het_han) {
        this.ngay_het_han = ngay_het_han;
    }

    public long getGiaTriDonToiThieu() {
        return giaTriDonToiThieu;
    }

    public void setGiaTriDonToiThieu(long giaTriDonToiThieu) {
        this.giaTriDonToiThieu = giaTriDonToiThieu;
    }

    public long getGiaTriGiamToiDa() {
        return giaTriGiamToiDa;
    }

    public void setGiaTriGiamToiDa(long giaTriGiamToiDa) {
        this.giaTriGiamToiDa = giaTriGiamToiDa;
    }

    public String getTrang_thai() {
        return Trang_thai;
    }

    public void setTrang_thai(String trang_thai) {
        Trang_thai = trang_thai;
    }
}
