package He_thong_quan_ly.demo.Repository.Admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import He_thong_quan_ly.demo.Module.Admin.SanPhamVariant_module;

public interface SanPhamVariantRepository
        extends JpaRepository<SanPhamVariant_module, String> {

    void deleteBySanPham_SanPhamId(String sanPhamId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SanPhamVariant_module v WHERE v.sanPham.sanPhamId = :sanPhamId")
    int deleteAllBySanPhamIdBulk(@Param("sanPhamId") String sanPhamId);

    List<SanPhamVariant_module> findBySanPham_SanPhamIdOrderBySizeAsc(String sanPhamId);

    List<SanPhamVariant_module> findBySanPham_SanPhamIdIn(List<String> sanPhamIds);

    Optional<SanPhamVariant_module> findFirstBySanPham_SanPhamIdAndSizeIgnoreCase(String sanPhamId, String size);

    Optional<SanPhamVariant_module> findFirstBySanPham_SanPhamIdOrderByPriceAsc(String sanPhamId);

}
