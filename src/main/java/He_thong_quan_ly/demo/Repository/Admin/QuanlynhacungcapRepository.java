package He_thong_quan_ly.demo.Repository.Admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import He_thong_quan_ly.demo.Module.Admin.NhaCungCap_module;

@Repository
public interface QuanlynhacungcapRepository
        extends JpaRepository<NhaCungCap_module, String> {

    @Query("SELECT ncc.nhacungcapId FROM NhaCungCap_module ncc ORDER BY ncc.nhacungcapId DESC")
    List<String> findTopNhaCungCapId(Pageable pageable);

    boolean existsByTenNhaCungCapIgnoreCase(String tenNhaCungCap);

    boolean existsByTenNhaCungCapIgnoreCaseAndNhacungcapIdNot(String tenNhaCungCap, String nhacungcapId);

}
