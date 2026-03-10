package He_thong_quan_ly.demo.Service.Admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.DanhMuc_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlydanhmucRepository;

@Service
public class QuanlydanhmucService {

    @Autowired
    private QuanlydanhmucRepository danhmucRepo;

    @Cacheable("allCategories")
    public List<DanhMuc_module> findAll() {
        return danhmucRepo.findAll();
    }

    public Page<DanhMuc_module> findAllPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        return danhmucRepo.findAll(PageRequest.of(pageIndex, pageSize));
    }

    @CacheEvict(value = "allCategories", allEntries = true)
    public boolean themDanhMuc(DanhMuc_module dm) {

        boolean trungTen = danhmucRepo.existsByTenDmIgnoreCase(
                dm.getTenDm().trim());

        if (trungTen) {
            return false;
        }

        dm.setDanhmucId(tuDongSinhMa());
        danhmucRepo.save(dm);
        return true;
    }

    private String tuDongSinhMa() {
        Pageable pageable = PageRequest.of(0, 1);
        List<String> list = danhmucRepo.findTopDanhMucId(pageable);

        if (list.isEmpty()) {
            return "DM001";
        }

        String lastId = list.get(0);
        int so = Integer.parseInt(lastId.substring(2));
        return String.format("DM%03d", so + 1);
    }

    public DanhMuc_module findById(String id) {
        return danhmucRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
    }

    @Transactional(readOnly = true)
    public DanhMuc_module getReferenceById(String id) {
        return danhmucRepo.getReferenceById(id);
    }

    @CacheEvict(value = "allCategories", allEntries = true)
    public void update(String id, String tenDm) {
        if (danhmucRepo.existsByTenDmIgnoreCaseAndDanhmucIdNot(tenDm.trim(), id)) {
            throw new RuntimeException("Sản phẩm đã tồn tại");
        }
        DanhMuc_module dm = danhmucRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        dm.setTenDm(tenDm);
        danhmucRepo.save(dm);
    }

    @CacheEvict(value = "allCategories", allEntries = true)
    public void delete(String id) {
        try {
            danhmucRepo.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "Không thể xóa danh mục vì đang được sử dụng");
        }
    }
}
