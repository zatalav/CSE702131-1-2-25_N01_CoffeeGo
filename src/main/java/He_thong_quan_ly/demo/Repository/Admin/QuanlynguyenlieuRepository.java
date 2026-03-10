package He_thong_quan_ly.demo.Repository.Admin;

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

}
