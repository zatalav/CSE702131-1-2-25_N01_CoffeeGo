package He_thong_quan_ly.demo.Repository.bang_phu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import He_thong_quan_ly.demo.Module.bang_phu.CungCap;
import He_thong_quan_ly.demo.Module.bang_phu_id.CungCapId;

public interface CungCapRepository
        extends JpaRepository<CungCap, CungCapId> {

    @Query("""
                    select cc
                    from CungCap cc
                    join fetch cc.nguyenLieu nl
                    join fetch cc.nhaCungCap ncc
            """)
    java.util.List<CungCap> findAllWithRefs();

    @Query("""
                select cc
                from CungCap cc
                join fetch cc.nhaCungCap ncc
                where cc.nguyenLieu.nguyenlieuId in :nguyenLieuIds
            """)
    java.util.List<CungCap> findByNguyenLieuIdsWithNhaCungCap(
            @Param("nguyenLieuIds") java.util.List<String> nguyenLieuIds);

    boolean existsByNguyenLieu_NguyenlieuIdAndNhaCungCap_NhacungcapId(
            String nguyenLieuId,
            String nhaCungCapId);

    void deleteByNguyenLieu_NguyenlieuId(String nguyenLieuId);

    @Query("""
                select count(cc) > 0
                from CungCap cc
                where lower(cc.nguyenLieu.tenNguyenLieu) = lower(:tenNguyenLieu)
                  and cc.nhaCungCap.nhacungcapId = :nccId
            """)
    boolean existsByTenNguyenLieuAndNhaCungCap(
            @Param("tenNguyenLieu") String tenNguyenLieu,
            @Param("nccId") String nccId);

    @Query("""
                    select count(cc) > 0
                    from CungCap cc
                    where lower(cc.nguyenLieu.tenNguyenLieu) = lower(:tenNguyenLieu)
                        and cc.nhaCungCap.nhacungcapId = :nccId
                        and cc.nguyenLieu.nguyenlieuId <> :nguyenLieuId
            """)
    boolean existsByTenNguyenLieuAndNhaCungCapExcludingNguyenLieu(
            @Param("tenNguyenLieu") String tenNguyenLieu,
            @Param("nccId") String nccId,
            @Param("nguyenLieuId") String nguyenLieuId);

}
