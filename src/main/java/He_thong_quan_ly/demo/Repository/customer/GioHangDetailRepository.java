package He_thong_quan_ly.demo.Repository.customer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import He_thong_quan_ly.demo.Module.bang_phu.giohang_detail;
import He_thong_quan_ly.demo.Module.bang_phu_id.giohang_detail_id;

public interface GioHangDetailRepository extends JpaRepository<giohang_detail, giohang_detail_id> {
    @Query("select d from giohang_detail d where d.giohang.GioHang_id = :gioHangId")
    List<giohang_detail> findByGioHangId(@Param("gioHangId") String gioHangId);

    @Query("select d from giohang_detail d where d.giohang.GioHang_id = :gioHangId and d.sanpham.sanPhamId = :sanPhamId and upper(coalesce(d.size, 'M')) = upper(coalesce(:size, 'M'))")
    Optional<giohang_detail> findByGioHangIdAndSanPhamIdAndSize(
            @Param("gioHangId") String gioHangId,
            @Param("sanPhamId") String sanPhamId,
            @Param("size") String size);

    @Query("select d from giohang_detail d where d.giohang.GioHang_id = :gioHangId and d.sanpham.sanPhamId = :sanPhamId")
    List<giohang_detail> findAllByGioHangIdAndSanPhamId(
            @Param("gioHangId") String gioHangId,
            @Param("sanPhamId") String sanPhamId);

    @Query("select count(d) from giohang_detail d where d.giohang.GioHang_id = :gioHangId")
    long countByGioHangId(@Param("gioHangId") String gioHangId);

    @Query("select coalesce(sum(d.soLuong), 0) from giohang_detail d where d.giohang.GioHang_id = :gioHangId")
    long sumQuantityByGioHangId(@Param("gioHangId") String gioHangId);
}
