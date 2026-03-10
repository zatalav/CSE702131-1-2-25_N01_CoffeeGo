package He_thong_quan_ly.demo.Repository.NhanVienKho;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import He_thong_quan_ly.demo.Module.NhanVienKho.KhobepId;
import He_thong_quan_ly.demo.Module.NhanVienKho.Khobep_module;

public interface KhoBepRepository extends JpaRepository<Khobep_module, KhobepId> {
    Optional<Khobep_module> findByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(
            String cosoId,
            String nguyenlieuId);

    Optional<Khobep_module> findFirstByNguyenLieu_NguyenlieuId(String nguyenlieuId);

    boolean existsByNguyenLieu_NguyenlieuId(String nguyenlieuId);

    List<Khobep_module> findAllByCoSo_CosoId(String cosoId);

    List<Khobep_module> findAllByCoSo_CosoIdAndNguyenLieu_NguyenlieuIdIn(String cosoId, List<String> nguyenlieuIds);

    boolean existsByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(String cosoId, String nguyenlieuId);
}
