package He_thong_quan_ly.demo.Repository.Admin;

import org.springframework.data.jpa.repository.JpaRepository;

import He_thong_quan_ly.demo.Module.Admin.CoSo_module;

public interface QuanlycosoRepository extends JpaRepository<CoSo_module, String> {
    CoSo_module findTopByOrderByCosoIdDesc();
}
