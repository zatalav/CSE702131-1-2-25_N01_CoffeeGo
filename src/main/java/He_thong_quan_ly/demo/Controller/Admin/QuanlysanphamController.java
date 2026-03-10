package He_thong_quan_ly.demo.Controller.Admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Config.CloudinaryViewConfigResolver;
import He_thong_quan_ly.demo.Module.Admin.DanhMuc_module;
import He_thong_quan_ly.demo.Module.Admin.SanPhamVariant_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.bang_phu.CongThuc;
import He_thong_quan_ly.demo.Repository.Admin.SanPhamVariantRepository;
import He_thong_quan_ly.demo.Service.Admin.QuanlydanhmucService;
import He_thong_quan_ly.demo.Service.Admin.QuanlynguyenlieuService;
import He_thong_quan_ly.demo.Service.Admin.QuanlysanphamService;

@Controller
@RequestMapping("/admin/sanpham")
public class QuanlysanphamController {

    private static final Logger logger = LoggerFactory.getLogger(QuanlysanphamController.class);

    private final QuanlysanphamService sanPhamService;
    private final QuanlynguyenlieuService nguyenLieuService;
    private final QuanlydanhmucService danhMucService;
    private final SanPhamVariantRepository variantRepository;
    private final CloudinaryViewConfigResolver cloudinaryViewConfigResolver;

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.upload-preset:coffee_upload}")
    private String cloudinaryUploadPreset;

    public QuanlysanphamController(
            QuanlysanphamService sanPhamService,
            QuanlynguyenlieuService nguyenLieuService,
            QuanlydanhmucService danhMucService,
            SanPhamVariantRepository variantRepository,
            CloudinaryViewConfigResolver cloudinaryViewConfigResolver) {

        this.sanPhamService = sanPhamService;
        this.nguyenLieuService = nguyenLieuService;
        this.danhMucService = danhMucService;
        this.variantRepository = variantRepository;
        this.cloudinaryViewConfigResolver = cloudinaryViewConfigResolver;
    }

    /* ================== HIỂN THỊ ================== */
    @GetMapping
    public String hienThiSanPham(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        final int pageSize = 10;
        Page<SanPham_module> productPage = sanPhamService.findAllPagedForAdmin(page, pageSize);

        model.addAttribute("dsSanPham", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("listDanhMuc", danhMucService.findAll());
        model.addAttribute("cloudinaryCloudName", cloudinaryViewConfigResolver.resolveCloudName(cloudinaryCloudName));
        model.addAttribute("cloudinaryUploadPreset", cloudinaryUploadPreset);

        model.addAttribute("sanPham", new SanPham_module());
        return "Admin/Quanlysanpham";
    }

    @GetMapping("/ingredients-lite")
    @ResponseBody
    public List<Map<String, String>> getIngredientLiteForProductForm() {
        return nguyenLieuService.findAllLiteForProductPage();
    }

    private record VariantInput(String size, double price) {
    }

    private List<VariantInput> normalizeVariantInputs(List<String> sizes, List<Double> prices) {
        if (sizes == null || prices == null || sizes.size() != prices.size()) {
            throw new IllegalArgumentException("Danh sách kích cỡ/giá không hợp lệ");
        }

        List<VariantInput> normalized = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            String rawSize = sizes.get(i);
            if (rawSize == null || rawSize.isBlank()) {
                continue;
            }
            Double rawPrice = prices.get(i);
            if (rawPrice == null || rawPrice <= 0) {
                throw new IllegalArgumentException("Giá theo kích cỡ phải lớn hơn 0");
            }
            String size = rawSize.trim().toUpperCase(Locale.ROOT);
            normalized.add(new VariantInput(size, rawPrice));
        }

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng thêm ít nhất 1 kích cỡ cho sản phẩm");
        }
        return normalized;
    }

    /* ================== ADD ================== */
    @PostMapping("/add")
    public String themSanPham(
            @ModelAttribute SanPham_module sanPham,
            @RequestParam("danhmucId") String danhmucId,

            @RequestParam("size[]") List<String> sizes,
            @RequestParam("price[]") List<Double> prices,

            @RequestParam("nguyenLieuId[]") List<String> nguyenLieuIds,
            @RequestParam("soLuong[]") List<Double> soLuongs,
            @RequestParam("donVi[]") List<String> donVis,
            RedirectAttributes redirect) {

        long requestStartNs = System.nanoTime();

        DanhMuc_module dm = danhMucService.getReferenceById(danhmucId);
        sanPham.setDanhMuc(dm);

        if (sanPhamService.existsByTenSp(sanPham.getTenSp())) {
            redirect.addFlashAttribute("errorMessage", "Tên sản phẩm đã tồn tại!");
            return "redirect:/admin/sanpham";
        }

        try {
            List<VariantInput> variantInputs = normalizeVariantInputs(sizes, prices);
            sanPham.setGia(Math.round(variantInputs.get(0).price()));

            List<SanPhamVariant_module> variants = new ArrayList<>(variantInputs.size());
            for (VariantInput input : variantInputs) {
                SanPhamVariant_module variant = new SanPhamVariant_module();
                variant.setVariantId(generateVariantId());
                variant.setSize(input.size());
                variant.setPrice(input.price());
                variants.add(variant);
            }

            long dbStartNs = System.nanoTime();
            sanPhamService.saveWithCongThucAndVariants(sanPham, nguyenLieuIds, soLuongs, donVis, variants);
            long dbMs = (System.nanoTime() - dbStartNs) / 1_000_000;

            long totalMs = (System.nanoTime() - requestStartNs) / 1_000_000;
            logger.info(
                    "[PERF][SANPHAM_ADD] total={}ms db={}ms variants={} recipeRows={} hasImage={}",
                    totalMs,
                    dbMs,
                    variants.size(),
                    nguyenLieuIds == null ? 0 : nguyenLieuIds.size(),
                    sanPham.getHinhAnh() != null && !sanPham.getHinhAnh().isBlank());

            redirect.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            logger.error("Thêm sản phẩm thất bại", ex);
            String message = ex.getMessage() == null || ex.getMessage().isBlank()
                    ? "Thêm sản phẩm thất bại!"
                    : ex.getMessage();
            redirect.addFlashAttribute("errorMessage", message);
        }

        return "redirect:/admin/sanpham";
    }

    /* ================== LOAD EDIT (AJAX) ================== */
    @GetMapping("/edit")
    @ResponseBody
    public Map<String, Object> getSanPhamEdit(@RequestParam String id) {

        SanPham_module sp = sanPhamService.findById(id);
        List<CongThuc> congThucs = sanPhamService.findCongThucBySanPhamId(id);

        Map<String, Object> res = new HashMap<>();
        res.put("id", sp.getSanPhamId());
        res.put("ten", sp.getTenSp());
        res.put("gia", sp.getGia());
        res.put("trangThai", sp.getTrangThai());
        res.put("moTa", sp.getMoTa());
        res.put("hinhAnh", sp.getHinhAnh());
        res.put("danhmucId", sp.getDanhMuc().getDanhmucId());

        res.put("congThucs", congThucs.stream().map(ct -> {
            Map<String, Object> m = new HashMap<>();
            m.put("nguyenLieuId", ct.getNguyenLieuId());
            m.put("soLuong", ct.getSoLuong());
            m.put("donVi", ct.getDonVi());
            return m;
        }).toList());

        res.put("variants", variantRepository.findBySanPham_SanPhamIdOrderBySizeAsc(id).stream().map(variant -> {
            Map<String, Object> m = new HashMap<>();
            m.put("size", variant.getSize());
            m.put("price", variant.getPrice());
            return m;
        }).toList());

        return res;
    }

    /* ================== UPDATE ================== */
    @PostMapping("/update")
    public String updateSanPham(
            @ModelAttribute SanPham_module sanPham,
            @RequestParam("danhmucId") String danhmucId,

            @RequestParam("size[]") List<String> sizes,
            @RequestParam("price[]") List<Double> prices,

            @RequestParam("nguyenLieuId[]") List<String> nguyenLieuIds,
            @RequestParam("soLuong[]") List<Double> soLuongs,
            @RequestParam("donVi[]") List<String> donVis,
            RedirectAttributes redirect) {

        long requestStartNs = System.nanoTime();

        DanhMuc_module dm = danhMucService.getReferenceById(danhmucId);
        sanPham.setDanhMuc(dm);

        try {
            List<VariantInput> variantInputs = normalizeVariantInputs(sizes, prices);
            sanPham.setGia(Math.round(variantInputs.get(0).price()));

            List<SanPhamVariant_module> variants = new ArrayList<>(variantInputs.size());
            for (VariantInput input : variantInputs) {
                SanPhamVariant_module variant = new SanPhamVariant_module();
                variant.setVariantId(generateVariantId());
                variant.setSize(input.size());
                variant.setPrice(input.price());
                variants.add(variant);
            }

            long dbStartNs = System.nanoTime();
            sanPhamService.updateWithCongThucAndVariants(sanPham, nguyenLieuIds, soLuongs, donVis, variants);
            long dbMs = (System.nanoTime() - dbStartNs) / 1_000_000;

            long totalMs = (System.nanoTime() - requestStartNs) / 1_000_000;
            logger.info(
                    "[PERF][SANPHAM_UPDATE] total={}ms db={}ms variants={} recipeRows={} hasImage={} sanPhamId={}",
                    totalMs,
                    dbMs,
                    variants.size(),
                    nguyenLieuIds == null ? 0 : nguyenLieuIds.size(),
                    sanPham.getHinhAnh() != null && !sanPham.getHinhAnh().isBlank(),
                    sanPham.getSanPhamId());

            redirect.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        } catch (Exception ex) {
            logger.error("Cập nhật sản phẩm thất bại, sanPhamId={}", sanPham.getSanPhamId(), ex);
            String message = ex.getMessage() == null || ex.getMessage().isBlank()
                    ? "Cập nhật sản phẩm thất bại!"
                    : ex.getMessage();
            redirect.addFlashAttribute("errorMessage", message);
        }

        return "redirect:/admin/sanpham";
    }

    /* ================== DELETE ================== */
    @PostMapping("/delete")
    public String deleteSanPham(
            @RequestParam("id") String sanPhamId,
            RedirectAttributes redirect) {

        sanPhamService.deleteById(sanPhamId);
        redirect.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        return "redirect:/admin/sanpham";
    }

    private String generateVariantId() {
        return "V" + UUID.randomUUID().toString().substring(0, 5);
    }

}
