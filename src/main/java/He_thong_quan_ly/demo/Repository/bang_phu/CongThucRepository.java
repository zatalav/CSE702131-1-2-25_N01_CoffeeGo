package He_thong_quan_ly.demo.Repository.bang_phu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.bang_phu.CongThuc;
import He_thong_quan_ly.demo.Module.bang_phu_id.CongThucId;

public interface CongThucRepository
                extends JpaRepository<CongThuc, CongThucId> {

        @Transactional(readOnly = true)
        List<CongThuc> findBySanPham_SanPhamId(String sanPhamId);

        // Prefer querying by the actual FK field (more reliable than the relationship
        // path)
        @Transactional(readOnly = true)
        List<CongThuc> findBySanPhamId(String sanPhamId);

        @Transactional(readOnly = true)
        List<CongThuc> findBySanPhamIdIn(List<String> sanPhamIds);

        void deleteBySanPham_SanPhamId(String sanPhamId);

        void deleteBySanPhamId(String sanPhamId);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("DELETE FROM CongThuc c WHERE c.sanPhamId = :sanPhamId")
        int deleteAllBySanPhamIdBulk(@Param("sanPhamId") String sanPhamId);
}
