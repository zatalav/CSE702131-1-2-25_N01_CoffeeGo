package He_thong_quan_ly.demo.Repository.Admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;

@Repository
public interface QuanlynhanvienRepository extends JpaRepository<NhanVien_module, String> {

    @Query(value = "SELECT * FROM nhan_vien WHERE gmail = :gmail ORDER BY nhanvien_id ASC LIMIT 1", nativeQuery = true)
    java.util.Optional<NhanVien_module> findByGmail(
            @org.springframework.data.repository.query.Param("gmail") String gmail);

    List<NhanVien_module> findByCoSo_CosoIdOrderByNhanvienIdAsc(String cosoId);

    @Query(value = """
            SELECT nv
            FROM NhanVien_module nv
            LEFT JOIN FETCH nv.coSo
            ORDER BY nv.nhanvienId DESC
            """, countQuery = """
            SELECT COUNT(nv)
            FROM NhanVien_module nv
            """)
    Page<NhanVien_module> findAllForAdminPaged(Pageable pageable);

    @Query("SELECT nv.nhanvienId FROM NhanVien_module nv ORDER BY nv.nhanvienId DESC")
    List<String> findTopNhanVienId(Pageable pageable);

    @Query("select count(nv) > 0 from NhanVien_module nv where lower(nv.gmail) = lower(:gmail)")
    boolean existsByGmailIgnoreCase(@Param("gmail") String gmail);

    @Query("""
            select count(nv) > 0
            from NhanVien_module nv
            where lower(nv.gmail) = lower(:gmail)
              and nv.nhanvienId <> :nhanvienId
            """)
    boolean existsByGmailIgnoreCaseAndNhanvienIdNot(
            @Param("gmail") String gmail,
            @Param("nhanvienId") String nhanvienId);
}
