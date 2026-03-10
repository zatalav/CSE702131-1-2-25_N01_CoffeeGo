package He_thong_quan_ly.demo.Service.Admin;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.CoSo_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlycosoRepository;

@Service
@Transactional(readOnly = true)
public class QuanlycosoService {

    private final QuanlycosoRepository cosoRepository;

    public QuanlycosoService(QuanlycosoRepository cosoRepository) {
        this.cosoRepository = cosoRepository;
    }

    @Cacheable("allBranches")
    public List<CoSo_module> findAll() {
        return cosoRepository.findAll();
    }

    public Page<CoSo_module> findAllPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        return cosoRepository.findAll(PageRequest.of(pageIndex, pageSize));
    }

    public CoSo_module findById(String id) {
        return cosoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cơ sở không tồn tại"));
    }

    @CacheEvict(value = "allBranches", allEntries = true)
    @Transactional
    public void create(CoSo_module coso) {
        if (coso.getCosoId() == null || coso.getCosoId().isBlank()) {
            coso.setCosoId(generateId());
        }
        cosoRepository.save(coso);
    }

    @CacheEvict(value = "allBranches", allEntries = true)
    @Transactional
    public void update(CoSo_module coso) {
        CoSo_module old = findById(coso.getCosoId());
        old.setTenCs(coso.getTenCs());
        old.setDiaChi(coso.getDiaChi());
        old.setSdt(coso.getSdt());
        cosoRepository.save(old);
    }

    @CacheEvict(value = "allBranches", allEntries = true)
    @Transactional
    public void delete(String id) {
        cosoRepository.deleteById(id);
    }

    private String generateId() {
        CoSo_module last = cosoRepository.findTopByOrderByCosoIdDesc();
        if (last == null || last.getCosoId() == null || last.getCosoId().isBlank()) {
            return "CS001";
        }
        String digits = last.getCosoId().replaceAll("\\D", "");
        if (digits.isBlank()) {
            return "CS001";
        }
        int number = Integer.parseInt(digits);
        return String.format("CS%03d", number + 1);
    }
}
