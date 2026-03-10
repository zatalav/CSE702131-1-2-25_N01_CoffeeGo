package He_thong_quan_ly.demo.Service.Admin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.Admin.NhaCungCap_module;
import He_thong_quan_ly.demo.Module.bang_phu.CungCap;
import He_thong_quan_ly.demo.Module.bang_phu_id.CungCapId;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhacungcapRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.CungCapRepository;

@Service
public class QuanlynguyenlieuService {

    @Autowired
    private QuanlynguyenlieuRepository nguyenLieuRepo;

    @Autowired
    private QuanlynhacungcapRepository nhaCungCapRepo;

    @Autowired
    private CungCapRepository cungCapRepo;

    public List<NguyenLieu_module> findAll() {
        return nguyenLieuRepo.findAll();
    }

    @Cacheable("ingredientLiteForProductPage")
    @Transactional(readOnly = true)
    public List<Map<String, String>> findAllLiteForProductPage() {
        return nguyenLieuRepo.findAllLiteForProductPage()
                .stream()
                .map(nl -> Map.of("id", nl.getId(), "ten", nl.getTen()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "ingredientPagedAdmin", key = "#page + '-' + #size")
    public Page<NguyenLieu_module> getAllNguyenLieuPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        var pageable = PageRequest.of(pageIndex, pageSize);
        Page<NguyenLieu_module> pageData = nguyenLieuRepo.findAll(pageable);

        List<NguyenLieu_module> content = pageData.getContent();
        List<String> nguyenLieuIds = content.stream().map(NguyenLieu_module::getNguyenlieuId).toList();
        Map<String, List<CungCap>> cungCapByNguyenLieuId = nguyenLieuIds.isEmpty()
                ? Map.of()
                : cungCapRepo.findByNguyenLieuIdsWithNhaCungCap(nguyenLieuIds)
                        .stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                cc -> cc.getNguyenLieu().getNguyenlieuId()));

        for (NguyenLieu_module nl : content) {
            List<CungCap> dsCungCap = cungCapByNguyenLieuId.getOrDefault(nl.getNguyenlieuId(), List.of());
            nl.setDsCungCap(dsCungCap);
            nl.setTrangThai(computeTrangThai(nl.getSlTon()));
            String tenNCC = dsCungCap.stream()
                    .map(cc -> cc.getNhaCungCap().getTenNhaCungCap())
                    .distinct()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            nl.setTenNhaCungCap(tenNCC);
        }

        return new PageImpl<>(content, pageable, pageData.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<NguyenLieu_module> getAllNguyenLieu() {
        List<NguyenLieu_module> list = nguyenLieuRepo.findAllWithNhaCungCap();

        for (NguyenLieu_module nl : list) {
            nl.setTrangThai(computeTrangThai(nl.getSlTon()));
            String tenNCC = nl.getDsCungCap()
                    .stream()
                    .map(cc -> cc.getNhaCungCap().getTenNhaCungCap())
                    .distinct()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            nl.setTenNhaCungCap(tenNCC);
        }
        return list;
    }

    @Transactional(readOnly = true)
    @Cacheable("allSuppliers")
    public List<NhaCungCap_module> getAllNhaCungCap() {
        return nhaCungCapRepo.findAll();
    }

    public NguyenLieu_module findById(String id) {
        return nguyenLieuRepo.findById(id).orElse(null);
    }

    private String computeTrangThai(int slTon) {
        if (slTon <= 0)
            return "Hết hàng";
        if (slTon < 10)
            return "Sắp hết hàng";
        return "Còn hàng";
    }

    private String generateNguyenLieuId() {
        NguyenLieu_module last = nguyenLieuRepo.findTopByOrderByNguyenlieuIdDesc();
        if (last == null || last.getNguyenlieuId() == null)
            return "NL001";

        String lastId = last.getNguyenlieuId();
        int number = Integer.parseInt(lastId.substring(2));
        return String.format("NL%03d", number + 1);
    }

    @Transactional
    @CacheEvict(value = { "ingredientLiteForProductPage", "ingredientPagedAdmin" }, allEntries = true)
    public void themNguyenLieu(
            NguyenLieu_module nguyenLieu,
            List<String> nhaCungCapIds) {

        if (nguyenLieu.getNguyenlieuId() == null || nguyenLieu.getNguyenlieuId().isBlank()) {
            nguyenLieu.setNguyenlieuId(generateNguyenLieuId());
        }
        nguyenLieu.setTrangThai(computeTrangThai(nguyenLieu.getSlTon()));

        if (nhaCungCapIds != null && !nhaCungCapIds.isEmpty()) {
            Set<String> uniqueIds = new LinkedHashSet<>(nhaCungCapIds);
            for (String nccId : uniqueIds) {
                if (nccId == null || nccId.isBlank())
                    continue;
                if (cungCapRepo.existsByTenNguyenLieuAndNhaCungCap(
                        nguyenLieu.getTenNguyenLieu(), nccId)) {
                    throw new RuntimeException("Sản phẩm đã tồn tại");
                }
            }
        }

        nguyenLieuRepo.save(nguyenLieu);

        if (nhaCungCapIds != null && !nhaCungCapIds.isEmpty()) {
            Set<String> uniqueIds = new LinkedHashSet<>(nhaCungCapIds);
            for (String nccId : uniqueIds) {
                if (nccId == null || nccId.isBlank())
                    continue;
                NhaCungCap_module ncc = nhaCungCapRepo.findById(nccId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
                CungCap cc = new CungCap();
                cc.setId(new CungCapId(nguyenLieu.getNguyenlieuId(), nccId));
                cc.setNguyenLieu(nguyenLieu);
                cc.setNhaCungCap(ncc);
                cungCapRepo.save(cc);
            }
        }
    }

    @Transactional
    @CacheEvict(value = { "ingredientLiteForProductPage", "ingredientPagedAdmin" }, allEntries = true)
    public void updateNguyenLieu(
            NguyenLieu_module nguyenLieu,
            List<String> nhaCungCapIds) {

        NguyenLieu_module old = nguyenLieuRepo.findById(nguyenLieu.getNguyenlieuId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));

        String tenNguyenLieu = nguyenLieu.getTenNguyenLieu();
        old.setTenNguyenLieu(tenNguyenLieu);
        old.setDonVi(nguyenLieu.getDonVi());
        old.setGiaNhap(nguyenLieu.getGiaNhap());
        old.setSlTon(nguyenLieu.getSlTon());
        old.setTrangThai(computeTrangThai(nguyenLieu.getSlTon()));
        if (nhaCungCapIds != null && !nhaCungCapIds.isEmpty()) {
            Set<String> uniqueIds = new LinkedHashSet<>(nhaCungCapIds);
            for (String nccId : uniqueIds) {
                if (nccId == null || nccId.isBlank())
                    continue;
                if (cungCapRepo.existsByTenNguyenLieuAndNhaCungCapExcludingNguyenLieu(
                        tenNguyenLieu, nccId, old.getNguyenlieuId())) {
                    throw new RuntimeException("Nguyên liệu đã tồn tại");
                }
            }
        }

        nguyenLieuRepo.save(old);

        cungCapRepo.deleteByNguyenLieu_NguyenlieuId(old.getNguyenlieuId());

        if (nhaCungCapIds != null && !nhaCungCapIds.isEmpty()) {
            Set<String> uniqueIds = new LinkedHashSet<>(nhaCungCapIds);
            for (String nccId : uniqueIds) {
                if (nccId == null || nccId.isBlank())
                    continue;
                NhaCungCap_module ncc = nhaCungCapRepo.findById(nccId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
                CungCap cc = new CungCap();
                cc.setId(new CungCapId(old.getNguyenlieuId(), nccId));
                cc.setNguyenLieu(old);
                cc.setNhaCungCap(ncc);
                cungCapRepo.save(cc);
            }
        }
    }

    @Transactional
    @CacheEvict(value = { "ingredientLiteForProductPage", "ingredientPagedAdmin" }, allEntries = true)
    public void deleteById(String id) {
        cungCapRepo.deleteByNguyenLieu_NguyenlieuId(id);
        nguyenLieuRepo.deleteById(id);
    }

    // các hàm thêm + sinh mã giữ nguyên
}
