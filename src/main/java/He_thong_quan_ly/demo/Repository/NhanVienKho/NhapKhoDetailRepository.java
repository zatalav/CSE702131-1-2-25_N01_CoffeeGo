package He_thong_quan_ly.demo.Repository.NhanVienKho;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.bang_phu.NhapKho_detail;
import He_thong_quan_ly.demo.Module.bang_phu_id.NhapKho_detail_id;

@Repository
public interface NhapKhoDetailRepository extends JpaRepository<NhapKho_detail, NhapKho_detail_id> {
    @Query("""
            select d
            from NhapKho_detail d
            left join fetch d.nguyenlieu nl
            left join fetch d.nhaCungCap ncc
            where d.nhapkho.nhapkhoId = :maPhieu
            """)
    List<NhapKho_detail> findByMaPhieu(@Param("maPhieu") String maPhieu);
}
