package He_thong_quan_ly.demo.Service.Admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.MaGiamGia_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlymagiamgiaRepository;

@Service
@Transactional(readOnly = true)
public class QuanlymagiamgiaService {

    @Autowired
    private QuanlymagiamgiaRepository repo;

    @Cacheable("allDiscountCodes")
    public List<MaGiamGia_module> getAll() {
        return repo.findAll();
    }

    public Page<MaGiamGia_module> getAllPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        return repo.findAll(PageRequest.of(pageIndex, pageSize));
    }

    @CacheEvict(value = "allDiscountCodes", allEntries = true)
    @Transactional
    public void themMaGiamGia(MaGiamGia_module mg) {
        // 1️⃣ TỰ SINH ID
        mg.setMagiamgia_id(tuDongSinhMa());
        mg.setGiaTriDonToiThieu(normalizeNonNegative(mg.getGiaTriDonToiThieu()));
        mg.setGiaTriGiamToiDa(normalizeNonNegative(mg.getGiaTriGiamToiDa()));

        // 2️⃣ MẶC ĐỊNH TRẠNG THÁI
        mg.setTrang_thai("HOAT_DONG");

        // 3️⃣ NẾU ĐÃ QUÁ HẠN → HẾT HẠN
        if (mg.getNgay_het_han() != null &&
                mg.getNgay_het_han().isBefore(LocalDate.now())) {
            mg.setTrang_thai("HET_HAN");
        }

        repo.save(mg);
    }

    @CacheEvict(value = "allDiscountCodes", allEntries = true)
    @Transactional
    public void update(MaGiamGia_module mg) {
        MaGiamGia_module old = repo.findById(mg.getMagiamgia_id())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

        old.setTen_ma_gg(mg.getTen_ma_gg());
        old.setGiam_gia(mg.getGiam_gia());
        old.setMo_ta(mg.getMo_ta());
        old.setNgay_het_han(mg.getNgay_het_han());
        old.setGiaTriDonToiThieu(normalizeNonNegative(mg.getGiaTriDonToiThieu()));
        old.setGiaTriGiamToiDa(normalizeNonNegative(mg.getGiaTriGiamToiDa()));

        // If expired, force HET_HAN regardless of selection
        if (old.getNgay_het_han() != null && old.getNgay_het_han().isBefore(LocalDate.now())) {
            old.setTrang_thai("HET_HAN");
        } else if (mg.getTrang_thai() != null) {
            old.setTrang_thai(mg.getTrang_thai());
        } else {
            old.setTrang_thai("HOAT_DONG");
        }

        repo.save(old);
    }

    @CacheEvict(value = "allDiscountCodes", allEntries = true)
    @Transactional
    public void delete(String id) {
        repo.deleteById(id);
    }

    private String tuDongSinhMa() {
        List<String> list = repo.findTopMaGiamGiaId(PageRequest.of(0, 1));
        if (list.isEmpty()) {
            return "MG001";
        }

        String lastId = list.get(0);
        int num = Integer.parseInt(lastId.substring(2));
        return String.format("MG%03d", num + 1);
    }

    private long normalizeNonNegative(long value) {
        return Math.max(0L, value);
    }
}
