package He_thong_quan_ly.demo.Service.Admin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.SanPhamVariant_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.bang_phu.CongThuc;
import He_thong_quan_ly.demo.Repository.Admin.QuanlysanphamRepository;
import He_thong_quan_ly.demo.Repository.Admin.SanPhamVariantRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.CongThucRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class QuanlysanphamService {

    private static final Logger logger = LoggerFactory.getLogger(QuanlysanphamService.class);

    private final QuanlysanphamRepository sanPhamRepository;
    private final CongThucRepository congThucRepository;
    private final SanPhamVariantRepository variantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public QuanlysanphamService(
            QuanlysanphamRepository sanPhamRepository,
            CongThucRepository congThucRepository,
            SanPhamVariantRepository variantRepository) {

        this.sanPhamRepository = sanPhamRepository;
        this.congThucRepository = congThucRepository;
        this.variantRepository = variantRepository;
    }

    /* ================== BASIC ================== */

    @Transactional(readOnly = true)
    public List<SanPham_module> findAll() {
        return sanPhamRepository.findAllForAdminList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "adminProductPage", key = "#page + '-' + #size")
    public Page<SanPham_module> findAllPagedForAdmin(int page, int size) {
        int pageIndex = Math.max(page, 0);
        int pageSize = Math.max(1, Math.min(size, 50));
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return sanPhamRepository.findAllForAdminList(pageable);
    }

    @Cacheable("activeProducts")
    public List<SanPham_module> findActiveProducts() {
        return sanPhamRepository.findActiveProducts();
    }

    @Cacheable(value = "activeProductsPaged", key = "#size")
    @Transactional(readOnly = true)
    public Page<SanPham_module> findActiveProductsPaged(int size) {
        int pageSize = Math.max(1, Math.min(size, 60));
        Pageable pageable = PageRequest.of(0, pageSize);
        return sanPhamRepository.findActiveProducts(pageable);
    }

    public boolean existsByTenSp(String tenSp) {
        return sanPhamRepository.existsByTenSp(tenSp);
    }

    public SanPham_module findById(String id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    /* ================== EDIT - LOAD DATA ================== */

    @Transactional(readOnly = true)
    public List<CongThuc> findCongThucBySanPhamId(String sanPhamId) {
        return congThucRepository.findBySanPhamId(sanPhamId);
    }

    /* ================== ID GENERATE ================== */

    private String generateSanPhamId() {
        // Generate ID locally to avoid querying the latest ID on remote DB.
        return "SP" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /* ================== ADD ================== */

    private static class CongThucItem {
        final String nguyenLieuId;
        double soLuong;
        String donVi;

        CongThucItem(String nguyenLieuId, double soLuong, String donVi) {
            this.nguyenLieuId = nguyenLieuId;
            this.soLuong = soLuong;
            this.donVi = donVi;
        }
    }

    private List<CongThucItem> normalizeCongThucInput(
            List<String> nguyenLieuIds,
            List<Double> soLuongs,
            List<String> donVis) {

        if (nguyenLieuIds == null || soLuongs == null || donVis == null)
            throw new IllegalArgumentException("Công thức không hợp lệ");
        if (nguyenLieuIds.size() != soLuongs.size() || nguyenLieuIds.size() != donVis.size())
            throw new IllegalArgumentException("Công thức không hợp lệ (sai số dòng)");

        // Merge duplicate nguyenLieuId to avoid duplicate composite PK (nguyenlieu_id,
        // sanpham_id)
        Map<String, CongThucItem> merged = new LinkedHashMap<>();
        for (int i = 0; i < nguyenLieuIds.size(); i++) {
            String nlId = nguyenLieuIds.get(i);
            if (nlId != null)
                nlId = nlId.trim();
            if (nlId == null || nlId.isBlank())
                continue;

            Double slObj = soLuongs.get(i);
            double sl = slObj == null ? 0.0 : slObj;
            String dv = donVis.get(i);

            CongThucItem existing = merged.get(nlId);
            if (existing == null) {
                merged.put(nlId, new CongThucItem(nlId, sl, dv));
                continue;
            }

            // If unit matches, sum; if unit differs, keep latest row (avoid throwing 500)
            if (existing.donVi != null && dv != null && existing.donVi.equals(dv)) {
                existing.soLuong += sl;
            } else {
                existing.soLuong = sl;
                existing.donVi = dv;
            }
        }

        return new ArrayList<>(merged.values());
    }

    @Transactional
    @CacheEvict(value = { "activeProducts", "activeProductsPaged", "adminProductPage" }, allEntries = true)
    public void saveWithCongThuc(
            SanPham_module sanPham,
            List<String> nguyenLieuIds,
            List<Double> soLuongs,
            List<String> donVis) {

        sanPham.setSanPhamId(generateSanPhamId());
        sanPham.setTrangThai("HOẠT ĐỘNG");
        sanPhamRepository.save(sanPham);

        List<CongThucItem> items = normalizeCongThucInput(nguyenLieuIds, soLuongs, donVis);
        List<CongThuc> congThucBatch = new ArrayList<>(items.size());
        for (CongThucItem item : items) {
            CongThuc ct = new CongThuc();
            // set both the owning FK field and the relation reference
            ct.setSanPhamId(sanPham.getSanPhamId());
            ct.setSanPham(sanPham);
            ct.setNguyenLieuId(item.nguyenLieuId);
            ct.setSoLuong(item.soLuong);
            ct.setDonVi(item.donVi);
            congThucBatch.add(ct);
        }
        congThucRepository.saveAll(congThucBatch);
    }

    @Transactional
    @CacheEvict(value = { "activeProducts", "activeProductsPaged", "adminProductPage" }, allEntries = true)
    public void saveWithCongThucAndVariants(
            SanPham_module sanPham,
            List<String> nguyenLieuIds,
            List<Double> soLuongs,
            List<String> donVis,
            List<SanPhamVariant_module> variants) {

        long totalStartNs = System.nanoTime();
        long saveProductMs;
        long saveRecipeMs;
        long saveVariantMs = 0;

        sanPham.setSanPhamId(generateSanPhamId());
        sanPham.setTrangThai("HOẠT ĐỘNG");
        long saveProductStartNs = System.nanoTime();
        entityManager.persist(sanPham);
        saveProductMs = (System.nanoTime() - saveProductStartNs) / 1_000_000;

        List<CongThucItem> items = normalizeCongThucInput(nguyenLieuIds, soLuongs, donVis);
        List<CongThuc> congThucBatch = new ArrayList<>(items.size());
        for (CongThucItem item : items) {
            CongThuc ct = new CongThuc();
            ct.setSanPhamId(sanPham.getSanPhamId());
            ct.setSanPham(sanPham);
            ct.setNguyenLieuId(item.nguyenLieuId);
            ct.setSoLuong(item.soLuong);
            ct.setDonVi(item.donVi);
            congThucBatch.add(ct);
        }
        long saveRecipeStartNs = System.nanoTime();
        for (CongThuc ct : congThucBatch) {
            entityManager.persist(ct);
        }
        saveRecipeMs = (System.nanoTime() - saveRecipeStartNs) / 1_000_000;

        if (variants != null && !variants.isEmpty()) {
            for (SanPhamVariant_module variant : variants) {
                variant.setSanPham(sanPham);
            }
            long saveVariantStartNs = System.nanoTime();
            for (SanPhamVariant_module variant : variants) {
                entityManager.persist(variant);
            }
            saveVariantMs = (System.nanoTime() - saveVariantStartNs) / 1_000_000;
        }

        long totalMs = (System.nanoTime() - totalStartNs) / 1_000_000;
        logger.info(
                "[PERF][SERVICE_ADD_DB] total={}ms saveProduct={}ms saveRecipe={}ms saveVariant={}ms recipeRows={} variantRows={}",
                totalMs,
                saveProductMs,
                saveRecipeMs,
                saveVariantMs,
                congThucBatch.size(),
                variants == null ? 0 : variants.size());
    }

    /* ================== UPDATE ================== */

    @Transactional
    @CacheEvict(value = { "activeProducts", "activeProductsPaged", "adminProductPage" }, allEntries = true)
    public void updateWithCongThuc(
            SanPham_module sanPham,
            List<String> nguyenLieuIds,
            List<Double> soLuongs,
            List<String> donVis) {

        if (sanPhamRepository.existsByTenSpIgnoreCaseAndSanPhamIdNot(
                sanPham.getTenSp(), sanPham.getSanPhamId())) {
            throw new IllegalArgumentException("Sản phẩm đã tồn tại");
        }

        SanPham_module old = sanPhamRepository
                .findById(sanPham.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        old.setTenSp(sanPham.getTenSp());
        old.setGia(sanPham.getGia());
        old.setMoTa(sanPham.getMoTa());
        old.setTrangThai(sanPham.getTrangThai());
        old.setDanhMuc(sanPham.getDanhMuc());
        if (sanPham.getHinhAnh() != null && !sanPham.getHinhAnh().isBlank()) {
            old.setHinhAnh(sanPham.getHinhAnh());
        }

        sanPhamRepository.save(old);

        congThucRepository.deleteAllBySanPhamIdBulk(old.getSanPhamId());

        List<CongThucItem> items = normalizeCongThucInput(nguyenLieuIds, soLuongs, donVis);
        List<CongThuc> congThucBatch = new ArrayList<>(items.size());
        for (CongThucItem item : items) {
            CongThuc ct = new CongThuc();
            // set FK value explicitly because the @ManyToOne is non-insertable/updatable
            ct.setSanPhamId(old.getSanPhamId());
            ct.setSanPham(old);
            ct.setNguyenLieuId(item.nguyenLieuId);
            ct.setSoLuong(item.soLuong);
            ct.setDonVi(item.donVi);
            congThucBatch.add(ct);
        }
        congThucRepository.saveAll(congThucBatch);
    }

    @Transactional
    @CacheEvict(value = { "activeProducts", "activeProductsPaged", "adminProductPage" }, allEntries = true)
    public void updateWithCongThucAndVariants(
            SanPham_module sanPham,
            List<String> nguyenLieuIds,
            List<Double> soLuongs,
            List<String> donVis,
            List<SanPhamVariant_module> variants) {

        long totalStartNs = System.nanoTime();
        long saveProductMs;
        long deleteRecipeMs;
        long saveRecipeMs;
        long deleteVariantMs;
        long saveVariantMs = 0;

        if (sanPhamRepository.existsByTenSpIgnoreCaseAndSanPhamIdNot(
                sanPham.getTenSp(), sanPham.getSanPhamId())) {
            throw new IllegalArgumentException("Sản phẩm đã tồn tại");
        }

        SanPham_module old = sanPhamRepository
                .findById(sanPham.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        old.setTenSp(sanPham.getTenSp());
        old.setGia(sanPham.getGia());
        old.setMoTa(sanPham.getMoTa());
        old.setTrangThai(sanPham.getTrangThai());
        old.setDanhMuc(sanPham.getDanhMuc());
        if (sanPham.getHinhAnh() != null && !sanPham.getHinhAnh().isBlank()) {
            old.setHinhAnh(sanPham.getHinhAnh());
        }
        long saveProductStartNs = System.nanoTime();
        sanPhamRepository.save(old);
        saveProductMs = (System.nanoTime() - saveProductStartNs) / 1_000_000;

        long deleteRecipeStartNs = System.nanoTime();
        congThucRepository.deleteAllBySanPhamIdBulk(old.getSanPhamId());
        deleteRecipeMs = (System.nanoTime() - deleteRecipeStartNs) / 1_000_000;
        List<CongThucItem> items = normalizeCongThucInput(nguyenLieuIds, soLuongs, donVis);
        List<CongThuc> congThucBatch = new ArrayList<>(items.size());
        for (CongThucItem item : items) {
            CongThuc ct = new CongThuc();
            ct.setSanPhamId(old.getSanPhamId());
            ct.setSanPham(old);
            ct.setNguyenLieuId(item.nguyenLieuId);
            ct.setSoLuong(item.soLuong);
            ct.setDonVi(item.donVi);
            congThucBatch.add(ct);
        }
        long saveRecipeStartNs = System.nanoTime();
        congThucRepository.saveAll(congThucBatch);
        saveRecipeMs = (System.nanoTime() - saveRecipeStartNs) / 1_000_000;

        long deleteVariantStartNs = System.nanoTime();
        variantRepository.deleteAllBySanPhamIdBulk(old.getSanPhamId());
        deleteVariantMs = (System.nanoTime() - deleteVariantStartNs) / 1_000_000;
        if (variants != null && !variants.isEmpty()) {
            for (SanPhamVariant_module variant : variants) {
                variant.setSanPham(old);
            }
            long saveVariantStartNs = System.nanoTime();
            variantRepository.saveAll(variants);
            saveVariantMs = (System.nanoTime() - saveVariantStartNs) / 1_000_000;
        }

        long totalMs = (System.nanoTime() - totalStartNs) / 1_000_000;
        logger.info(
                "[PERF][SERVICE_UPDATE_DB] total={}ms saveProduct={}ms delRecipe={}ms saveRecipe={}ms delVariant={}ms saveVariant={}ms recipeRows={} variantRows={} sanPhamId={}",
                totalMs,
                saveProductMs,
                deleteRecipeMs,
                saveRecipeMs,
                deleteVariantMs,
                saveVariantMs,
                congThucBatch.size(),
                variants == null ? 0 : variants.size(),
                old.getSanPhamId());
    }

    /* ================== DELETE ================== */

    @Transactional
    @CacheEvict(value = { "activeProducts", "activeProductsPaged", "adminProductPage" }, allEntries = true)
    public void deleteById(String sanPhamId) {
        congThucRepository.deleteBySanPhamId(sanPhamId);
        sanPhamRepository.deleteById(sanPhamId);
    }
}
