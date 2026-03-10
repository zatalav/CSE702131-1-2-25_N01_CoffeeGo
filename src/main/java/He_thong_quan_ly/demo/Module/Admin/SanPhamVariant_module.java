package He_thong_quan_ly.demo.Module.Admin;

import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "san_pham_variant", indexes = {
        @Index(name = "idx_spvariant_sanpham", columnList = "sanpham_id"),
        @Index(name = "idx_spvariant_sanpham_size", columnList = "sanpham_id,size"),
        @Index(name = "idx_spvariant_sanpham_price", columnList = "sanpham_id,price")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamVariant_module {
    @Id
    @Column(name = "variant_id")
    private String variantId;

    @ManyToOne
    @JoinColumn(name = "sanpham_id")
    private SanPham_module sanPham;

    @Column(name = "size")
    private String size;

    @Column(name = "price")
    private double price;
}
