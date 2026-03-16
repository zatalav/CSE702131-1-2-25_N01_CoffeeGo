package He_thong_quan_ly.demo.Repository.NhanVienKho;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.NhanVienKho.XuatKho_module;

@Repository
public interface XuatKhoRepository extends JpaRepository<XuatKho_module, String> {

    interface DateCountView {
        LocalDate getNgay();

        Long getTotal();
    }

    @Query(value = "SELECT xuatkho_id FROM xuat_kho ORDER BY xuatkho_id DESC LIMIT 1", nativeQuery = true)
    String findLatestId();

    @Query("""
            SELECT x
            FROM XuatKho_module x
                LEFT JOIN FETCH x.nhanVien
                ORDER BY x.ngayXuat DESC
            """)
    List<XuatKho_module> findAllOrderByNgayXuatDesc();

    @EntityGraph(attributePaths = { "nhanVien" })
    @Query("""
            SELECT x
            FROM XuatKho_module x
            ORDER BY x.ngayXuat DESC
            """)
    List<XuatKho_module> findRecent(Pageable pageable);

    @Query("""
            SELECT x.ngayXuat AS ngay, COUNT(x) AS total
            FROM XuatKho_module x
            WHERE x.ngayXuat BETWEEN :fromDate AND :toDate
            GROUP BY x.ngayXuat
            """)
    List<DateCountView> countByDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @EntityGraph(attributePaths = { "nhanVien" })
    @Query("""
            SELECT x
            FROM XuatKho_module x
            WHERE (:keyword IS NULL OR :keyword = ''
                OR lower(x.xuatkhoId) LIKE lower(concat('%', :keyword, '%'))
                OR lower(coalesce(x.ghiChu, '')) LIKE lower(concat('%', :keyword, '%'))
                OR lower(coalesce(x.nhanVien.tenNv, '')) LIKE lower(concat('%', :keyword, '%')))
            """)
    Page<XuatKho_module> findPaged(@Param("keyword") String keyword, Pageable pageable);
}
