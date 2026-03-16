package He_thong_quan_ly.demo.Controller.NhanVien;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.DTO.OrderCancelRequest;
import He_thong_quan_ly.demo.Module.DTO.OrderCreateRequest;
import He_thong_quan_ly.demo.Service.Admin.QuanlydanhmucService;
import He_thong_quan_ly.demo.Service.Admin.QuanlydonhangService;
import He_thong_quan_ly.demo.Service.Admin.QuanlysanphamService;
import He_thong_quan_ly.demo.Util.PdfFontUtil;
import He_thong_quan_ly.demo.Util.PerfStatsWindow;

@Controller
@RequestMapping("/nhanvien")
public class DonhangController {

    private static final Logger log = LoggerFactory.getLogger(DonhangController.class);
    private static final PerfStatsWindow DONHANG_LATENCY = new PerfStatsWindow(100);

    private final QuanlysanphamService sanPhamService;
    private final QuanlydanhmucService danhMucService;
    private final QuanlydonhangService donhangService;

    public DonhangController(
            QuanlysanphamService sanPhamService,
            QuanlydanhmucService danhMucService,
            QuanlydonhangService donhangService) {
        this.sanPhamService = sanPhamService;
        this.danhMucService = danhMucService;
        this.donhangService = donhangService;
    }

    @GetMapping("/donhang")
    public String taoDonHang(Model model, Authentication authentication) {
        long startedAt = System.nanoTime();
        String username = authentication == null ? null : authentication.getName();
        List<SanPham_module> dsSanPham = donhangService.getAvailableProductsForNhanVien(
                username,
                sanPhamService.findActiveProducts());

        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("dsDanhMuc", danhMucService.findAll());
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        PerfStatsWindow.Snapshot s = DONHANG_LATENCY.addAndSnapshot(elapsedMs);
        log.info("[PERF] GET /nhanvien/donhang user={} products={} elapsed={}ms p95={}ms p99={}ms n={}",
                username,
                dsSanPham.size(),
                elapsedMs,
                s.p95(),
                s.p99(),
                s.count());
        return "NhanVien/Donhang";
    }

    @GetMapping("/lichsu-donhang")
    public String lichSuDonHang(Model model, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        model.addAttribute("orders", donhangService.getHistoryRowsForNhanVien(username));
        return "NhanVien/Lichsudonhang";
    }

    @GetMapping("/donhang-khach")
    public String donHangKhach(Model model, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        model.addAttribute("orders", donhangService.getPendingCustomerOrders(username));
        return "NhanVien/DonhangKhach";
    }

    @PostMapping("/donhang-khach/confirm")
    public String xacNhanDonKhach(
            @RequestParam("orderId") String orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication == null ? null : authentication.getName();
            donhangService.confirmCustomerOrder(orderId, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "\u0110\u00e3 x\u00e1c nh\u1eadn \u0111\u01a1n h\u00e0ng " + orderId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Kh\u00f4ng th\u1ec3 x\u00e1c nh\u1eadn \u0111\u01a1n h\u00e0ng"
                            : ex.getMessage());
        }
        return "redirect:/nhanvien/donhang-khach";
    }

    @PostMapping(value = "/donhang/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> taiPdf(
            @RequestBody OrderCreateRequest request,
            Authentication authentication) throws java.io.IOException {
        String orderId;
        try {
            orderId = donhangService.taoDonHang(
                    authentication == null ? null : authentication.getName(),
                    request);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ex.getMessage() == null ? new byte[0]
                            : ex.getMessage().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Document document = new Document(PageSize.A4, 28f, 28f, 30f, 30f)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = PdfFontUtil.titleFont(16);
            Font normalFont = PdfFontUtil.normalFont(11);

            Paragraph title = new Paragraph("PHI\u1ebeU \u0110\u01a0N H\u00c0NG", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph time = new Paragraph("Ng\u00e0y t\u1ea1o: " + timestamp, normalFont);
            time.setSpacingAfter(10);
            document.add(time);

            Paragraph orderCode = new Paragraph("M\u00e3 \u0111\u01a1n h\u00e0ng: " + orderId, normalFont);
            orderCode.setSpacingAfter(8);
            document.add(orderCode);

            PdfPTable table = new PdfPTable(new float[] { 4f, 1.2f, 1.6f, 2f, 3f });
            table.setSpacingBefore(10);
            table.setWidthPercentage(100);

            addHeaderCell(table, "S\u1ea3n ph\u1ea9m");
            addHeaderCell(table, "SL");
            addHeaderCell(table, "\u0110\u01a1n gi\u00e1");
            addHeaderCell(table, "Th\u00e0nh ti\u1ec1n");
            addHeaderCell(table, "Y\u00eau c\u1ea7u");

            long subtotal = 0L;
            if (request.getItems() != null) {
                for (var item : request.getItems()) {
                    long lineTotal = item.getPrice() * item.getQty();
                    subtotal += lineTotal;

                    table.addCell(new Phrase(item.getName(), normalFont));
                    table.addCell(new Phrase(String.valueOf(item.getQty()), normalFont));
                    table.addCell(new Phrase(formatCurrency(item.getPrice()), normalFont));
                    table.addCell(new Phrase(formatCurrency(lineTotal), normalFont));
                    table.addCell(new Phrase(buildCustomNote(item), normalFont));
                }
            }

            document.add(table);

            Paragraph summary = new Paragraph(
                    "T\u1ea1m t\u00ednh: " + formatCurrency(subtotal),
                    normalFont);
            summary.setSpacingBefore(12);
            document.add(summary);

            if (request.getNote() != null && !request.getNote().isBlank()) {
                Paragraph note = new Paragraph("Ghi ch\u00fa: " + request.getNote(), normalFont);
                note.setSpacingBefore(8);
                document.add(note);
            }

            document.close();

            String fileName = "don-hang-" + System.currentTimeMillis() + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(outputStream.toByteArray());
        }
    }

    @PostMapping(value = "/donhang/cancel/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> huyDonPdf(@RequestBody OrderCancelRequest request)
            throws java.io.IOException {
        String orderId = request.getOrderId();
        String reason = request.getReason();
        DonHang_module donhang;

        try {
            donhang = donhangService.huyDon(orderId, reason);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ex.getMessage().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Document document = new Document(PageSize.A4, 28f, 28f, 30f, 30f)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = PdfFontUtil.titleFont(16);
            Font normalFont = PdfFontUtil.normalFont(11);

            Paragraph title = new Paragraph("H\u00d3A \u0110\u01a0N H\u1ee6Y", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph time = new Paragraph("Ng\u00e0y h\u1ee7y: " + timestamp, normalFont);
            time.setSpacingAfter(10);
            document.add(time);

            document.add(new Paragraph("M\u00e3 \u0111\u01a1n h\u00e0ng: " + orderId, normalFont));
            document.add(new Paragraph("Tr\u1ea1ng th\u00e1i: \u0110\u00e3 h\u1ee7y", normalFont));
            document.add(new Paragraph("L\u00fd do: " + (reason == null ? "" : reason), normalFont));
            long tongTien = java.util.Optional.ofNullable(donhang.getTong_tien()).orElse(0L);
            document.add(new Paragraph("T\u1ed5ng ti\u1ec1n: " + formatCurrency(tongTien), normalFont));

            document.close();

            String fileName = "huy-don-" + orderId + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(outputStream.toByteArray());
        }
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

    private String formatCurrency(long value) {
        return java.text.NumberFormat
                .getCurrencyInstance(java.util.Locale.forLanguageTag("vi-VN"))
                .format(value);
    }

    private String buildCustomNote(He_thong_quan_ly.demo.Module.DTO.OrderItemRequest item) {
        StringBuilder builder = new StringBuilder();
        if (item.getSize() != null && !item.getSize().isBlank()) {
            builder.append("Size: ").append(item.getSize());
        }
        if (item.getSugar() != null && !item.getSugar().isBlank()) {
            appendSeparator(builder);
            builder.append("\u0110\u01b0\u1eddng: ").append(item.getSugar());
        }
        if (item.getIce() != null && !item.getIce().isBlank()) {
            appendSeparator(builder);
            builder.append("\u0110\u00e1: ").append(item.getIce());
        }
        if (item.getMilk() != null && !item.getMilk().isBlank()) {
            appendSeparator(builder);
            builder.append("S\u1eefa: ").append(item.getMilk());
        }
        if (item.getNote() != null && !item.getNote().isBlank()) {
            if (builder.length() > 0)
                appendSeparator(builder);
            builder.append("Ghi ch\u00fa: ").append(item.getNote());
        }

        return builder.length() == 0 ? "-" : builder.toString();
    }

    private void appendSeparator(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(" | ");
        }
    }

}
