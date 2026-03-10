package He_thong_quan_ly.demo.Repository.Admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import He_thong_quan_ly.demo.Module.Admin.SanPham_module;

public interface QuanlysanphamRepository
                extends JpaRepository<SanPham_module, String> {

        boolean existsByTenSp(String tenSp);

        boolean existsByTenSpIgnoreCaseAndSanPhamIdNot(String tenSp, String sanPhamId);

        @Query("""
                        SELECT sp.sanPhamId
                        FROM SanPham_module sp
                        ORDER BY sp.sanPhamId DESC
                        """)
        List<String> findAllIdDesc(Pageable pageable);

        @Query("""
                        SELECT sp
                        FROM SanPham_module sp
                        LEFT JOIN FETCH sp.danhMuc
                        ORDER BY sp.sanPhamId DESC
                        """)
        List<SanPham_module> findAllForAdminList();

        @Query(value = """
                        SELECT sp
                        FROM SanPham_module sp
                        LEFT JOIN FETCH sp.danhMuc
                        ORDER BY sp.sanPhamId DESC
                        """, countQuery = """
                        SELECT COUNT(sp)
                        FROM SanPham_module sp
                        """)
        Page<SanPham_module> findAllForAdminList(Pageable pageable);

        /*
         * ⭐ Load sản phẩm + danh mục (tránh lazy lỗi)
         */
        @Query("""
                        SELECT DISTINCT sp
                        FROM SanPham_module sp
                        LEFT JOIN FETCH sp.danhMuc
                        WHERE sp.trangThai IS NOT NULL
                          AND (
                                lower(sp.trangThai) LIKE '%hoạt động%'
                             OR lower(sp.trangThai) LIKE '%hoat dong%'
                             OR lower(sp.trangThai) LIKE '%hoat_dong%'
                             OR lower(sp.trangThai) LIKE '%hoat-dong%'
                             OR lower(sp.trangThai) LIKE '%active%'
                          )
                        """)
        List<SanPham_module> findActiveProducts();

        /*
         * ⭐ PHÂN TRANG KHÔNG ĐƯỢC FETCH COLLECTION
         * Hibernate cấm join fetch list khi paging
         */
        @Query("""
                        SELECT sp
                        FROM SanPham_module sp
                        LEFT JOIN sp.danhMuc dm
                        WHERE sp.trangThai IS NOT NULL
                          AND (
                                lower(sp.trangThai) LIKE '%hoạt động%'
                             OR lower(sp.trangThai) LIKE '%hoat dong%'
                             OR lower(sp.trangThai) LIKE '%hoat_dong%'
                             OR lower(sp.trangThai) LIKE '%hoat-dong%'
                             OR lower(sp.trangThai) LIKE '%active%'
                          )
                        """)
        Page<SanPham_module> findActiveProducts(Pageable pageable);

}
