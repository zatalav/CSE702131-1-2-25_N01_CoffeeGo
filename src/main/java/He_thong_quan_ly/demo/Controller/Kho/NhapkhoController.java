package He_thong_quan_ly.demo.Controller.Kho;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import He_thong_quan_ly.demo.Repository.NhanVienKho.NhapKhoDetailRepository;
import He_thong_quan_ly.demo.Service.Common.CurrentUserInfoService;
import He_thong_quan_ly.demo.Service.Kho.KhoMasterDataService;
import He_thong_quan_ly.demo.Service.Kho.NhapKhoService;
import He_thong_quan_ly.demo.Util.PdfFontUtil;
import He_thong_quan_ly.demo.Util.PerfStatsWindow;

@Controller
@RequestMapping("/kho")
public class NhapkhoController {

    private static final Logger log = LoggerFactory.getLogger(NhapkhoController.class);
    private static final PerfStatsWindow NHAPKHO_LATENCY = new PerfStatsWindow(100);

    private final NhapKhoService nhapKhoService;
    private final NhapKhoDetailRepository nhapKhoDetailRepository;
    private final KhoMasterDataService khoMasterDataService;
    private final CurrentUserInfoService currentUserInfoService;

    public NhapkhoController(
            NhapKhoService nhapKhoService,
            NhapKhoDetailRepository nhapKhoDetailRepository,
            KhoMasterDataService khoMasterDataService,
            CurrentUserInfoService currentUserInfoService) {
        this.nhapKhoService = nhapKhoService;
        this.nhapKhoDetailRepository = nhapKhoDetailRepository;
        this.khoMasterDataService = khoMasterDataService;
        this.currentUserInfoService = currentUserInfoService;
    }

    @GetMapping("/nhapkho")
    public String nhapKho(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "q", required = false) String keyword,
            Model model,
            Authentication authentication) {
        long startedAt = System.nanoTime();
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        String normalizedKeyword = Optional.ofNullable(keyword).map(String::trim).orElse("");
        var pageData = nhapKhoService.findPaged(pageIndex, pageSize, normalizedKeyword);

        model.addAttribute("dsNhapKho", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", normalizedKeyword);
        addUserInfo(model, authentication);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        String username = authentication == null ? null : authentication.getName();
        PerfStatsWindow.Snapshot s = NHAPKHO_LATENCY.addAndSnapshot(elapsedMs);
        log.info("[PERF] GET /kho/nhapkho user={} elapsed={}ms p95={}ms p99={}ms n={}",
                username,
                elapsedMs,
                s.p95(),
                s.p99(),
                s.count());
        return "kho/Nhapkho";
    }

    @GetMapping("/nhapkho/tao")
    public String taoPhieuNhapKho(Model model, Authentication authentication) {
        model.addAttribute("maPhieu", nhapKhoService.generateNextId());
        model.addAttribute("today", java.time.LocalDate.now());
        try {
            Map<String, Object> masterData = khoMasterDataService.getMasterData();
            model.addAttribute("dsNguyenLieuJs", masterData.getOrDefault("dsNguyenLieuJs", List.of()));
            model.addAttribute("dsNhaCungCapJs", masterData.getOrDefault("dsNhaCungCapJs", List.of()));
            model.addAttribute("mapNguyenLieuNcc", masterData.getOrDefault("mapNguyenLieuNcc", Map.of()));
            model.addAttribute("mapTenNguyenLieuNcc", masterData.getOrDefault("mapTenNguyenLieuNcc", Map.of()));
            model.addAttribute("mapNccTenNguyenLieu", masterData.getOrDefault("mapNccTenNguyenLieu", Map.of()));
        } catch (Exception ex) {
            log.warn("Unable to preload kho master-data for nhap kho form", ex);
            model.addAttribute("dsNguyenLieuJs", List.of());
            model.addAttribute("dsNhaCungCapJs", List.of());
            model.addAttribute("mapNguyenLieuNcc", Map.of());
            model.addAttribute("mapTenNguyenLieuNcc", Map.of());
            model.addAttribute("mapNccTenNguyenLieu", Map.of());
        }
        addUserInfo(model, authentication);
        return "kho/TaoPhieuNhapKho";
    }

    @GetMapping("/master-data")
    public ResponseEntity<Map<String, Object>> masterData() {
        return ResponseEntity.ok(khoMasterDataService.getMasterData());
    }

    @PostMapping("/nhapkho/create")
    public String taoPhieuNhapKhoSubmit(
            @RequestParam("maPhieu") String maPhieu,
            @RequestParam("ngayNhap") LocalDate ngayNhap,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            @RequestParam("nguyenLieuId[]") List<String> nguyenLieuIds,
            @RequestParam("nhacungcapId[]") List<String> nhaCungCapIds,
            @RequestParam("donVi[]") List<String> donVis,
            @RequestParam("soLuong[]") List<Integer> soLuongs,
            @RequestParam("giaNhap[]") List<Long> giaNhaps,
            @RequestParam(value = "hanSuDung[]", required = false) List<LocalDate> hanSuDungs,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication == null ? null : authentication.getName();
            nhapKhoService.taoPhieuNhap(
                    username,
                    maPhieu,
                    ngayNhap,
                    ghiChu,
                    nguyenLieuIds,
                    nhaCungCapIds,
                    donVis,
                    soLuongs,
                    giaNhaps,
                    hanSuDungs);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm phiếu nhập kho thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Thêm phiếu nhập kho thất bại!" : ex.getMessage());
        }
        return "redirect:/kho/nhapkho";
    }

    @PostMapping(value = "/nhapkho/export", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPhieuNhap(@RequestBody List<String> ids) throws java.io.IOException {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var dsNhap = nhapKhoService.findByIds(ids);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Document document = new Document(PageSize.A4, 28f, 28f, 30f, 30f)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = PdfFontUtil.titleFont(16);
            Font normalFont = PdfFontUtil.normalFont(11);

            Paragraph title = new Paragraph("DANH SÁCH PHIẾU NHẬP KHO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            PdfPTable table = new PdfPTable(new float[] { 2f, 2f, 3f, 2f });
            table.setWidthPercentage(100);

            addHeaderCell(table, "Mã phiếu");
            addHeaderCell(table, "Ngày nhập");
            addHeaderCell(table, "Người nhập");
            addHeaderCell(table, "Tổng tiền");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (var nk : dsNhap) {
                String ngay = nk.getNgayNhap() == null ? "" : nk.getNgayNhap().format(formatter);
                String nv = nk.getNhanvien() == null ? "" : nk.getNhanvien().getTenNv();
                String tong = String.format("%,d đ", nk.getTongTien());

                table.addCell(new Phrase(nk.getNhapkhoId(), normalFont));
                table.addCell(new Phrase(ngay, normalFont));
                table.addCell(new Phrase(nv, normalFont));
                table.addCell(new Phrase(tong, normalFont));
            }

            document.add(table);
            document.close();

            String fileName = "phieu-nhap-kho-" + System.currentTimeMillis() + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(outputStream.toByteArray());
        }
    }

    @GetMapping("/nhapkho/detail")
    public ResponseEntity<List<java.util.Map<String, Object>>> nhapKhoDetail(
            @RequestParam("maPhieu") String maPhieu) {
        if (maPhieu == null || maPhieu.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        var details = nhapKhoDetailRepository.findByMaPhieu(maPhieu);
        var result = details.stream().map(d -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("tenNguyenLieu", d.getNguyenlieu() == null ? "" : d.getNguyenlieu().getTenNguyenLieu());
            Integer soLuong = d.getSoLuong();
            m.put("soLuong", soLuong == null ? 0 : soLuong);
            m.put("donVi", d.getDonVi() == null || d.getDonVi().isBlank()
                    ? (d.getNguyenlieu() == null ? "" : d.getNguyenlieu().getDonVi())
                    : d.getDonVi());
            m.put("nhaCungCap", d.getNhaCungCap() == null ? "" : d.getNhaCungCap().getTenNhaCungCap());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/nhapkho/update")
    public String capNhatPhieuNhap(
            @RequestParam("maPhieu") String maPhieu,
            @RequestParam(value = "ngayNhap", required = false) LocalDate ngayNhap,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes) {
        try {
            nhapKhoService.capNhatPhieuNhap(maPhieu, ngayNhap, ghiChu);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phiếu nhập kho thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Cập nhật phiếu nhập kho thất bại!" : ex.getMessage());
        }
        return "redirect:/kho/nhapkho";
    }

    @PostMapping("/nhapkho/delete")
    public String xoaPhieuNhap(
            @RequestParam("maPhieu") String maPhieu,
            RedirectAttributes redirectAttributes) {
        try {
            nhapKhoService.xoaPhieuNhap(maPhieu);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phiếu nhập kho thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Xóa phiếu nhập kho thất bại!" : ex.getMessage());
        }
        return "redirect:/kho/nhapkho";
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font headerFont = PdfFontUtil.headerFont(11);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new java.awt.Color(245, 238, 232));
        cell.setBorderColor(new java.awt.Color(210, 210, 210));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addUserInfo(Model model, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        if (username == null) {
            return;
        }
        var brief = currentUserInfoService.getBrief(username);
        if (!brief.isEmpty()) {
            model.addAttribute("currentUserName", brief.get("name"));
            model.addAttribute("currentUserRole", brief.get("role"));
        }
    }
}
