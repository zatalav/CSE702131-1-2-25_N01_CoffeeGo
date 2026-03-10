package He_thong_quan_ly.demo.Service.Admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.NhaCungCap_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhacungcapRepository;

@Service
@Transactional(readOnly = true)
public class QuanlynhacungcapService {

    @Autowired
    private QuanlynhacungcapRepository repo;

    @Cacheable("allSuppliers")
    public List<NhaCungCap_module> getAll() {
        return repo.findAll();
    }

    public Page<NhaCungCap_module> getAllPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        return repo.findAll(PageRequest.of(pageIndex, pageSize));
    }

    @CacheEvict(value = "allSuppliers", allEntries = true)
    @Transactional
    public boolean themNhaCungCap(NhaCungCap_module ncc) {

        boolean trungTen = repo.existsByTenNhaCungCapIgnoreCase(
                ncc.getTenNhaCungCap().trim());

        if (trungTen) {
            return false;
        }

        ncc.setNhacungcapId(tuDongSinhMa());
        repo.save(ncc);
        return true;
    }

    private String tuDongSinhMa() {
        Pageable pageable = PageRequest.of(0, 1);
        List<String> list = repo.findTopNhaCungCapId(pageable);

        if (list.isEmpty()) {
            return "NCC001";
        }

        int so = Integer.parseInt(list.get(0).substring(3));
        return String.format("NCC%03d", so + 1);
    }

    @CacheEvict(value = "allSuppliers", allEntries = true)
    @Transactional
    public void update(NhaCungCap_module ncc) {
        if (!repo.existsById(ncc.getNhacungcapId())) {
            throw new RuntimeException("Nhà cung cấp không tồn tại");
        }
        if (repo.existsByTenNhaCungCapIgnoreCaseAndNhacungcapIdNot(
                ncc.getTenNhaCungCap().trim(), ncc.getNhacungcapId())) {
            throw new RuntimeException("Sản phẩm đã tồn tại");
        }
        repo.save(ncc);
    }

    @CacheEvict(value = "allSuppliers", allEntries = true)
    @Transactional
    public void delete(String id) {
        try {
            repo.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "Không thể xóa nhà cung cấp vì đang được sử dụng");
        }
    }
}
