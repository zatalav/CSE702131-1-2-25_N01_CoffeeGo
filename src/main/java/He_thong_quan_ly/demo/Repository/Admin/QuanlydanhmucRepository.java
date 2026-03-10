package He_thong_quan_ly.demo.Repository.Admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.Admin.DanhMuc_module;

@Repository
public interface QuanlydanhmucRepository
        extends JpaRepository<DanhMuc_module, String> {

    @Query("SELECT dm.danhmucId FROM DanhMuc_module dm ORDER BY dm.danhmucId DESC")
    List<String> findTopDanhMucId(Pageable pageable);

    boolean existsByTenDmIgnoreCase(String tenDm);

    boolean existsByTenDmIgnoreCaseAndDanhmucIdNot(String tenDm, String danhmucId);
}
