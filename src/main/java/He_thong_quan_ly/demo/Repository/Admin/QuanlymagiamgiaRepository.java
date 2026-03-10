package He_thong_quan_ly.demo.Repository.Admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.Admin.MaGiamGia_module;

@Repository
public interface QuanlymagiamgiaRepository
        extends JpaRepository<MaGiamGia_module, String> {

    @Query("SELECT m.magiamgia_id FROM MaGiamGia_module m ORDER BY m.magiamgia_id DESC")
    List<String> findTopMaGiamGiaId(Pageable pageable);
}
