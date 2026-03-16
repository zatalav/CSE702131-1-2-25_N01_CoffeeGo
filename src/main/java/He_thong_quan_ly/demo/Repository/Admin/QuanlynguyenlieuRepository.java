package He_thong_quan_ly.demo.Repository.Admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;

public interface QuanlynguyenlieuRepository
        extends JpaRepository<NguyenLieu_module, String> {

    interface NguyenLieuLiteView {
        String getId();

        String getTen();
    }

    NguyenLieu_module findTopByOrderByNguyenlieuIdDesc();

    @Query("""
                SELECT DISTINCT nl
                FROM NguyenLieu_module nl
                LEFT JOIN FETCH nl.dsCungCap cc
                LEFT JOIN FETCH cc.nhaCungCap
            """)
    List<NguyenLieu_module> findAllWithNhaCungCap();

    @Query("""
                SELECT nl.nguyenlieuId AS id, nl.tenNguyenLieu AS ten
                FROM NguyenLieu_module nl
                ORDER BY nl.nguyenlieuId
            """)
    List<NguyenLieuLiteView> findAllLiteForProductPage();

    @Query("""
                SELECT nl
                FROM NguyenLieu_module nl
                WHERE (:keyword IS NULL OR :keyword = ''
                    OR lower(nl.nguyenlieuId) LIKE lower(concat('%', :keyword, '%'))
                    OR lower(nl.tenNguyenLieu) LIKE lower(concat('%', :keyword, '%'))
                    OR lower(coalesce(nl.trangThai, '')) LIKE lower(concat('%', :keyword, '%')))
            """)
    Page<NguyenLieu_module> findPagedForKho(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(nl.slTon), 0)
            FROM NguyenLieu_module nl
            """)
    long sumSlTon();

    @Query("""
            SELECT COUNT(nl)
            FROM NguyenLieu_module nl
            WHERE nl.slTon > 0 AND nl.slTon < :threshold
            """)
    long countLowStock(@Param("threshold") int threshold);

    @Query("""
            SELECT COUNT(nl)
            FROM NguyenLieu_module nl
            WHERE nl.hanSuDung IS NOT NULL
              AND nl.hanSuDung BETWEEN :fromDate AND :toDate
            """)
    long countExpiryBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("""
            SELECT nl
            FROM NguyenLieu_module nl
            WHERE nl.slTon <= :threshold
            ORDER BY nl.slTon ASC
            """)
    List<NguyenLieu_module> findTopLowStock(@Param("threshold") int threshold, Pageable pageable);

    @Query("""
            SELECT nl
            FROM NguyenLieu_module nl
            ORDER BY
                CASE WHEN nl.hanSuDung IS NULL THEN 1 ELSE 0 END,
                nl.hanSuDung ASC
            """)
    List<NguyenLieu_module> findTopByEarliestExpiry(Pageable pageable);

    @Query("""
                SELECT nl
                FROM NguyenLieu_module nl
                ORDER BY nl.slTon ASC
            """)
    List<NguyenLieu_module> findTopBySlTonAsc(Pageable pageable);

}
