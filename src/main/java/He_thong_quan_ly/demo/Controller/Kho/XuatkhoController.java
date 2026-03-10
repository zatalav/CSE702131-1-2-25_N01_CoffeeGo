package He_thong_quan_ly.demo.Controller.Kho;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import He_thong_quan_ly.demo.Repository.NhanVienKho.XuatKhoChiTietRepository;
import He_thong_quan_ly.demo.Service.Admin.QuanlycosoService;
import He_thong_quan_ly.demo.Service.Common.CurrentUserInfoService;
import He_thong_quan_ly.demo.Service.Kho.XuatKhoService;
import He_thong_quan_ly.demo.Util.PdfFontUtil;

@Controller
@RequestMapping("/kho")
public class XuatkhoController {

    private final XuatKhoService xuatKhoService;
    private final QuanlycosoService quanlycosoService;
    private final XuatKhoChiTietRepository xuatKhoChiTietRepository;
    private final CurrentUserInfoService currentUserInfoService;

    public XuatkhoController(XuatKhoService xuatKhoService,
            QuanlycosoService quanlycosoService,
            XuatKhoChiTietRepository xuatKhoChiTietRepository,
            CurrentUserInfoService currentUserInfoService) {
        this.xuatKhoService = xuatKhoService;
        this.quanlycosoService = quanlycosoService;
        this.xuatKhoChiTietRepository = xuatKhoChiTietRepository;
        this.currentUserInfoService = currentUserInfoService;
    }

    @GetMapping("/xuatkho")
    public String xuatKho(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "q", required = false) String keyword,
            Model model,
            Authentication authentication) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        String normalizedKeyword = Optional.ofNullable(keyword).map(String::trim).orElse("");
        var pageData = xuatKhoService.findPaged(pageIndex, pageSize, normalizedKeyword);

        model.addAttribute("dsXuatKho", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", normalizedKeyword);
        addUserInfo(model, authentication);
        return "kho/Xuatkho";
    }

    @GetMapping("/xuatkho/tao")
    public String taoPhieuXuatKho(Model model, Authentication authentication) {
        model.addAttribute("maPhieu", xuatKhoService.generateNextId());
        model.addAttribute("today", java.time.LocalDate.now());
        model.addAttribute("dsCoSo", quanlycosoService.findAll());
        addUserInfo(model, authentication);
        return "kho/TaoPhieuXuatKho";
    }

    @PostMapping("/xuatkho/create")
    public String taoPhieuXuatKhoSubmit(
            @RequestParam("maPhieu") String maPhieu,
            @RequestParam("cosoId") String cosoId,
            @RequestParam("ngayXuat") LocalDate ngayXuat,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            @RequestParam("nguyenLieuId[]") List<String> nguyenLieuIds,
            @RequestParam(value = "soLuong[]", required = false) List<Integer> soLuongs,
            @RequestParam(value = "giaNhap[]", required = false) List<Long> giaNhaps,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication == null ? null : authentication.getName();
            xuatKhoService.taoPhieuXuat(
                    username,
                    cosoId,
                    ngayXuat,
                    ghiChu,
                    nguyenLieuIds,
                    soLuongs,
                    giaNhaps);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm phiếu xuất kho thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Thêm phiếu xuất kho thất bại!" : ex.getMessage());
        }
        return "redirect:/kho/xuatkho";
    }

    @PostMapping(value = "/xuatkho/export", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPhieuXuat(@RequestBody List<String> ids) throws java.io.IOException {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var dsXuat = xuatKhoService.findAllByIds(ids);
        var chiTietList = xuatKhoChiTietRepository.findByXuatKhoIds(ids);
        var cosoIds = chiTietList.stream()
                .map(He_thong_quan_ly.demo.Module.NhanVienKho.XuatKhoChiTiet::getCosoId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        java.util.Map<String, String> mapCosoName = quanlycosoService.findAll().stream()
                .filter(cs -> cosoIds.contains(cs.getCosoId()))
                .collect(java.util.stream.Collectors.toMap(
                        He_thong_quan_ly.demo.Module.Admin.CoSo_module::getCosoId,
                        He_thong_quan_ly.demo.Module.Admin.CoSo_module::getTenCs));

        java.util.Map<String, He_thong_quan_ly.demo.Module.NhanVienKho.XuatKho_module> mapXuat = dsXuat.stream()
                .collect(java.util.stream.Collectors
                        .toMap(He_thong_quan_ly.demo.Module.NhanVienKho.XuatKho_module::getXuatkhoId, x -> x));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Document document = new Document(PageSize.A4, 28f, 28f, 30f, 30f)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = PdfFontUtil.titleFont(16);
            Font normalFont = PdfFontUtil.normalFont(11);

            Paragraph title = new Paragraph("DANH SÁCH PHIẾU XUẤT KHO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            PdfPTable table = new PdfPTable(new float[] { 2f, 2f, 2.5f, 3f, 2f, 2f });
            table.setWidthPercentage(100);

            addHeaderCell(table, "Mã phiếu");
            addHeaderCell(table, "Ngày xuất");
            addHeaderCell(table, "Tên nhân viên");
            addHeaderCell(table, "Nguyên liệu");
            addHeaderCell(table, "Số lượng");
            addHeaderCell(table, "Cơ sở");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (var ct : chiTietList) {
                String maPhieu = ct.getXuatKho() == null ? (ct.getId() == null ? "" : ct.getId().getXuatkhoId())
                        : ct.getXuatKho().getXuatkhoId();
                var xuat = mapXuat.get(maPhieu);
                String ngay = xuat == null || xuat.getNgayXuat() == null ? "" : xuat.getNgayXuat().format(formatter);
                String nv = xuat == null || xuat.getNhanvien() == null ? "" : xuat.getNhanvien().getTenNv();
                String nl = ct.getNguyenLieu() == null ? "" : ct.getNguyenLieu().getTenNguyenLieu();
                String soLuong = String.valueOf(ct.getSoLuong());
                String cosoName = ct.getCosoId() == null ? ""
                        : mapCosoName.getOrDefault(ct.getCosoId(), ct.getCosoId());

                table.addCell(new Phrase(maPhieu, normalFont));
                table.addCell(new Phrase(ngay, normalFont));
                table.addCell(new Phrase(nv, normalFont));
                table.addCell(new Phrase(nl, normalFont));
                table.addCell(new Phrase(soLuong, normalFont));
                table.addCell(new Phrase(cosoName, normalFont));
            }

            document.add(table);
            document.close();

            String fileName = "phieu-xuat-kho-" + System.currentTimeMillis() + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(outputStream.toByteArray());
        }
    }

    @GetMapping("/xuatkho/detail")
    public ResponseEntity<List<java.util.Map<String, Object>>> xuatKhoDetail(
            @RequestParam("maPhieu") String maPhieu) {
        if (maPhieu == null || maPhieu.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        var details = xuatKhoChiTietRepository.findByXuatKhoIds(java.util.List.of(maPhieu));
        var result = details.stream().map(d -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("tenNguyenLieu", d.getNguyenLieu() == null ? "" : d.getNguyenLieu().getTenNguyenLieu());
            m.put("soLuong", d.getSoLuong());
            m.put("donVi", d.getNguyenLieu() == null ? "" : d.getNguyenLieu().getDonVi());
            m.put("coSo", d.getCosoId() == null ? "" : d.getCosoId());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/xuatkho/update")
    public String capNhatPhieuXuat(
            @RequestParam("maPhieu") String maPhieu,
            @RequestParam(value = "ngayXuat", required = false) LocalDate ngayXuat,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes) {
        try {
            xuatKhoService.capNhatPhieuXuat(maPhieu, ngayXuat, ghiChu);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phiếu xuất kho thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Cập nhật phiếu xuất kho thất bại!" : ex.getMessage());
        }
        return "redirect:/kho/xuatkho";
    }

    @PostMapping("/xuatkho/delete")
    public String xoaPhieuXuat(
            @RequestParam("maPhieu") String maPhieu,
            RedirectAttributes redirectAttributes) {
        try {
            xuatKhoService.xoaPhieuXuat(maPhieu);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phiếu xuất kho thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Xóa phiếu xuất kho thất bại!" : ex.getMessage());
        }
        return "redirect:/kho/xuatkho";
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

    @GetMapping("/tonkho")
    public String danhSachTonKho(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "q", required = false) String keyword,
            Model model,
            Authentication authentication) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        String normalizedKeyword = Optional.ofNullable(keyword).map(String::trim).orElse("");
        var pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.ASC, "nguyenlieuId"));
        var pageData = xuatKhoService.findTonKhoPaged(pageable, normalizedKeyword);

        model.addAttribute("dsNguyenLieu", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", normalizedKeyword);
        addUserInfo(model, authentication);
        return "kho/Danhsachtonkho";
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
