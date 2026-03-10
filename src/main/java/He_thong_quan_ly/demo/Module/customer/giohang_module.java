package He_thong_quan_ly.demo.Module.customer;

import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gio_hang", indexes = {
        @Index(name = "idx_giohang_khachhang", columnList = "khachhang_id")
})
public class giohang_module {
    @Id
    @jakarta.persistence.Column(name = "gio_hang_id")
    private String GioHang_id;

    @OneToOne
    @JoinColumn(name = "khachhang_id")
    private KhachHang_module khachHang;
}
