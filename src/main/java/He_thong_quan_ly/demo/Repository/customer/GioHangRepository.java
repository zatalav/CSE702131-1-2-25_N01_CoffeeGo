package He_thong_quan_ly.demo.Repository.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.customer.giohang_module;

@Repository
public interface GioHangRepository extends JpaRepository<giohang_module, String> {
    @Query("select g from giohang_module g where g.khachHang.khachhang_id = :khachhangId")
    Optional<giohang_module> findByKhachHangId(@Param("khachhangId") String khachhangId);

    @Query("select max(g.GioHang_id) from giohang_module g")
    String findMaxId();
}