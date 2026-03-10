package He_thong_quan_ly.demo.Repository.Admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.Admin.DonHang_module;

@Repository
public interface QuanlydonhangRepository extends JpaRepository<DonHang_module, String> {

    @Query("""
            	SELECT d.donhang_id
            	FROM DonHang_module d
            	ORDER BY d.donhang_id DESC
            """)
    List<String> findAllIdDesc();

    @Query("""
                SELECT d
                FROM DonHang_module d
                WHERE d.nhanVien.nhanvienId = :nhanvienId
                ORDER BY d.Ngay_dat DESC
            """)
    List<DonHang_module> findByNhanVienIdOrderByNgayDatDesc(String nhanvienId);

    @Query("""
                SELECT d
                FROM DonHang_module d
                WHERE d.khachHang.khachhang_id = :khachhangId
                ORDER BY d.Ngay_dat DESC
            """)
    List<DonHang_module> findByKhachHangIdOrderByNgayDatDesc(String khachhangId);

    @Query("""
                SELECT COUNT(d)
                FROM DonHang_module d
                WHERE d.khachHang.khachhang_id = :khachhangId
            """)
    long countByKhachHangId(@Param("khachhangId") String khachhangId);

    @Query("""
                    SELECT d
                    FROM DonHang_module d
                    WHERE (d.Phan_loai = 'Online' OR d.Phan_loai = 'KH')
                        AND d.Trang_thai = :status
                    ORDER BY d.Ngay_dat DESC
            """)
    List<DonHang_module> findCustomerOrdersByStatus(@Param("status") String status);

    @Query("""
                    SELECT d
                    FROM DonHang_module d
                                        LEFT JOIN FETCH d.khachHang
                    WHERE (d.Phan_loai = 'Online' OR d.Phan_loai = 'KH')
                        AND d.Trang_thai = :status
                        AND d.nhanVien.nhanvienId = :nhanvienId
                    ORDER BY d.Ngay_dat DESC
            """)
    List<DonHang_module> findCustomerOrdersByStatusAndNhanVienId(
            @Param("status") String status,
            @Param("nhanvienId") String nhanvienId);

    @Query("""
                    SELECT d
                    FROM DonHang_module d
                                        LEFT JOIN FETCH d.khachHang
                    WHERE (d.Phan_loai = 'Online' OR d.Phan_loai = 'KH')
                        AND d.Trang_thai IN :statuses
                        AND d.nhanVien.nhanvienId = :nhanvienId
                    ORDER BY d.Ngay_dat DESC
            """)
    List<DonHang_module> findCustomerOrdersByStatusesAndNhanVienId(
            @Param("statuses") List<String> statuses,
            @Param("nhanvienId") String nhanvienId);

    @Query("""
                    SELECT d
                    FROM DonHang_module d
                                        LEFT JOIN FETCH d.khachHang
                    WHERE (d.Phan_loai = 'Online' OR d.Phan_loai = 'KH')
                        AND d.Trang_thai = 'Đã xác nhận'
                    ORDER BY d.Ngay_dat DESC
            """)
    List<DonHang_module> findConfirmedCustomerOrdersForDelivery();

    @Query("""
                            SELECT COUNT(d)
                            FROM DonHang_module d
                            WHERE (d.Phan_loai = 'Online' OR d.Phan_loai = 'KH')
                                    AND d.Trang_thai = 'Đã xác nhận'
            """)
    long countConfirmedCustomerOrdersForDelivery();

    @Query("""
                    SELECT d
                    FROM DonHang_module d
                                        LEFT JOIN FETCH d.khachHang
                    WHERE d.nhanVien.nhanvienId = :nhanvienId
                        AND d.Trang_thai = :status
                    ORDER BY d.Ngay_dat DESC
            """)
    List<DonHang_module> findByNhanVienIdAndStatusOrderByNgayDatDesc(
            @Param("nhanvienId") String nhanvienId,
            @Param("status") String status);

    @Query("""
                    SELECT COUNT(d)
                    FROM DonHang_module d
                    WHERE d.nhanVien.nhanvienId = :nhanvienId
                        AND d.Trang_thai = :status
            """)
    long countByNhanVienIdAndStatus(
            @Param("nhanvienId") String nhanvienId,
            @Param("status") String status);

    @Query("""
                    SELECT d.Trang_thai, COUNT(d)
                    FROM DonHang_module d
                    WHERE d.nhanVien.nhanvienId = :nhanvienId
                        AND FUNCTION('YEAR', d.Ngay_dat) = :year
                        AND (:month IS NULL OR FUNCTION('MONTH', d.Ngay_dat) = :month)
                    GROUP BY d.Trang_thai
            """)
    List<Object[]> countStatusByNhanVienAndPeriod(
            @Param("nhanvienId") String nhanvienId,
            @Param("year") int year,
            @Param("month") Integer month);

    @Query("""
                SELECT d
                FROM DonHang_module d
                LEFT JOIN FETCH d.khachHang
                ORDER BY d.Ngay_dat DESC
            """)
    List<DonHang_module> findRecentOrders(Pageable pageable);

    @Query(value = """
                SELECT d
                FROM DonHang_module d
                LEFT JOIN FETCH d.khachHang
                LEFT JOIN FETCH d.nhanVien
                ORDER BY d.Ngay_dat DESC
            """, countQuery = """
                SELECT COUNT(d)
                FROM DonHang_module d
            """)
    Page<DonHang_module> findAllForAdminPaged(Pageable pageable);
}
