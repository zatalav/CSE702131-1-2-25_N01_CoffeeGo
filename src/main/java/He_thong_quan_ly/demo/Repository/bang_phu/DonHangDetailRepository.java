package He_thong_quan_ly.demo.Repository.bang_phu;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.bang_phu.DonHang_detail;
import He_thong_quan_ly.demo.Module.bang_phu_id.donhang_detail_id;

@Repository
public interface DonHangDetailRepository extends JpaRepository<DonHang_detail, donhang_detail_id> {
    @Query("select d from DonHang_detail d where d.donhang.donhang_id = :donhangId")
    List<DonHang_detail> findByDonhangId(@Param("donhangId") String donhangId);

    @Query("""
            select d.sanPham.tenSp, sum(d.SL)
            from DonHang_detail d
            where FUNCTION('YEAR', d.donhang.Ngay_dat) = :year
              and FUNCTION('MONTH', d.donhang.Ngay_dat) = :month
              and (
            d.donhang.Trang_thai is null
            or (
                lower(d.donhang.Trang_thai) not like '%hủy%'
                and lower(d.donhang.Trang_thai) not like '%huy%'
                and lower(d.donhang.Trang_thai) not like '%cancel%'
            )
              )
            group by d.sanPham.tenSp
            order by sum(d.SL) desc
            """)
    List<Object[]> findTopProductSalesByMonth(
            @Param("year") int year,
            @Param("month") int month,
            Pageable pageable);
}
