package He_thong_quan_ly.demo.Repository.NhanVienKho;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import He_thong_quan_ly.demo.Module.NhanVienKho.XuatKhoChiTiet;
import He_thong_quan_ly.demo.Module.NhanVienKho.XuatKhoChiTietId;

public interface XuatKhoChiTietRepository extends JpaRepository<XuatKhoChiTiet, XuatKhoChiTietId> {
    @Query("""
            select ct
            from XuatKhoChiTiet ct
            left join fetch ct.nguyenLieu nl
            left join fetch ct.xuatKho xk
            where ct.xuatKho.xuatkhoId in :ids
            """)
    List<XuatKhoChiTiet> findByXuatKhoIds(@Param("ids") List<String> xuatKhoIds);
}
