package He_thong_quan_ly.demo.Repository.NhanVienKho;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.NhanVienKho.NhapKho_module;

@Repository
public interface NhapKhoRepository extends JpaRepository<NhapKho_module, String> {

    @Query(value = "SELECT nhapkho_id FROM nhap_kho ORDER BY nhapkho_id DESC LIMIT 1", nativeQuery = true)
    String findLatestId();

    @Query("""
            SELECT nk
            FROM NhapKho_module nk
                LEFT JOIN FETCH nk.nhanVien
                ORDER BY nk.ngayNhap DESC
            """)
    List<NhapKho_module> findAllOrderByNgayNhapDesc();

    @EntityGraph(attributePaths = { "nhanvien" })
    @Query("""
            SELECT nk
            FROM NhapKho_module nk
            WHERE (:keyword IS NULL OR :keyword = ''
                OR lower(nk.nhapkhoId) LIKE lower(concat('%', :keyword, '%'))
                OR lower(coalesce(nk.ghiChu, '')) LIKE lower(concat('%', :keyword, '%'))
                OR lower(coalesce(nk.nhanVien.tenNv, '')) LIKE lower(concat('%', :keyword, '%')))
            """)
    Page<NhapKho_module> findPaged(@Param("keyword") String keyword, Pageable pageable);
}
