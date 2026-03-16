package He_thong_quan_ly.demo.Repository.Admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.Admin.MaGiamGia_module;

@Repository
public interface QuanlymagiamgiaRepository
        extends JpaRepository<MaGiamGia_module, String> {

    @Query("SELECT m.magiamgia_id FROM MaGiamGia_module m ORDER BY m.magiamgia_id DESC")
    List<String> findTopMaGiamGiaId(Pageable pageable);

    @Query("""
            SELECT m
            FROM MaGiamGia_module m
            WHERE upper(coalesce(m.Trang_thai, '')) = 'HOAT_DONG'
                AND (m.ngay_het_han IS NULL OR m.ngay_het_han >= :now)
            """)
    List<MaGiamGia_module> findActiveVouchers(LocalDate now);
}
