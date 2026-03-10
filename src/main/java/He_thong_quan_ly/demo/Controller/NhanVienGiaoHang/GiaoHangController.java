package He_thong_quan_ly.demo.Controller.NhanVienGiaoHang;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import He_thong_quan_ly.demo.Repository.bang_phu.DonHangDetailRepository;
import He_thong_quan_ly.demo.Service.Admin.QuanlydonhangService;

@Controller
@RequestMapping("/nhanvien/giaohang")
public class GiaoHangController {

    private final QuanlydonhangService donhangService;
    private final DonHangDetailRepository donhangDetailRepository;

    public GiaoHangController(
            QuanlydonhangService donhangService,
            DonHangDetailRepository donhangDetailRepository) {
        this.donhangService = donhangService;
        this.donhangDetailRepository = donhangDetailRepository;
    }

    @GetMapping
    public String giaoHangDashboard(Model model, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        Map<String, Long> stats = donhangService.getDeliveryDashboardStats(username);
        model.addAttribute("stats", stats);
        model.addAttribute("availableOrders", donhangService.getConfirmedOrdersForDelivery());
        return "NhanVienGiaoHang/GiaoHangDashboard";
    }

    @GetMapping("/chart-data")
    @ResponseBody
    public Map<String, Object> getChartData(
            Authentication authentication,
            @RequestParam(name = "period", defaultValue = "month") String period,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month) {
        String username = authentication == null ? null : authentication.getName();
        LocalDate now = LocalDate.now();

        String normalizedPeriod = "year".equalsIgnoreCase(period) ? "year" : "month";
        int safeYear = year == null || year < 2000 ? now.getYear() : year;
        Integer safeMonth = null;
        if ("month".equals(normalizedPeriod)) {
            int currentMonth = now.getMonthValue();
            int chosen = month == null ? currentMonth : month;
            safeMonth = Math.max(1, Math.min(12, chosen));
        }

        Map<String, Long> data = donhangService.getDeliveryStatusSummary(username, safeYear, safeMonth);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("period", normalizedPeriod);
        response.put("year", safeYear);
        response.put("month", safeMonth);
        response.put("data", data);
        return response;
    }

    @GetMapping("/donhang")
    public String giaoHangDonHang(Model model, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        model.addAttribute("availableOrders", donhangService.getConfirmedOrdersForDelivery());
        model.addAttribute("myOrders", donhangService.getOrdersInDelivery(username));
        model.addAttribute("cancelledOrders", donhangService.getCancelledDeliveryOrders(username));
        return "NhanVienGiaoHang/GiaoHangOrders";
    }

    @PostMapping("/nhan")
    public String nhanDonGiaoHang(
            @RequestParam("orderId") String orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication == null ? null : authentication.getName();
            donhangService.takeOrderForDelivery(orderId, username);
            redirectAttributes.addFlashAttribute("successMessage", "Đã nhận đơn hàng " + orderId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Không thể nhận đơn hàng" : ex.getMessage());
        }
        return "redirect:/nhanvien/giaohang/donhang";
    }

    @GetMapping("/chitiet")
    public String chiTietGiaoHang(
            @RequestParam("orderId") String orderId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication == null ? null : authentication.getName();
            var order = donhangService.getDeliveryOrderDetail(orderId, username);

            String address = order.getDia_chi_giao() == null || order.getDia_chi_giao().isBlank()
                    ? (order.getKhachHang() == null || order.getKhachHang().getDia_chi() == null
                            ? ""
                            : order.getKhachHang().getDia_chi())
                    : order.getDia_chi_giao();
            String mapQuery = address == null || address.isBlank()
                    ? ""
                    : URLEncoder.encode(address, StandardCharsets.UTF_8);
            String mapUrl = mapQuery.isBlank() ? "" : "https://www.google.com/maps?q=" + mapQuery;

            model.addAttribute("order", order);
            model.addAttribute("paymentLabel", resolvePaymentLabel(order));
            model.addAttribute("customerNote", resolveCustomerNote(order));
            model.addAttribute("orderItems", donhangDetailRepository.findByDonhangId(orderId));
            model.addAttribute("mapUrl", mapUrl);
            model.addAttribute("mapQuery", mapQuery);
            return "NhanVienGiaoHang/GiaoHangDetail";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Không thể xem chi tiết đơn hàng" : ex.getMessage());
            return "redirect:/nhanvien/giaohang/donhang";
        }
    }

    private String resolvePaymentLabel(DonHang_module order) {
        if (order == null) {
            return "";
        }
        String method = order.getPaymentMethod() == null ? ""
                : order.getPaymentMethod().trim().toUpperCase(Locale.ROOT);
        String status = order.getPaymentStatus() == null ? ""
                : order.getPaymentStatus().trim().toUpperCase(Locale.ROOT);
        String note = order.getGhi_chu() == null ? "" : order.getGhi_chu().trim();
        String normalizedNote = normalizeForMatch(note);

        if ("COD".equals(method) || "CHUA_THANH_TOAN".equals(status)) {
            return "Thanh toán khi nhận hàng (COD)";
        }
        if (method.isBlank() && normalizedNote.contains("THANH TOAN: COD")) {
            return "Thanh toán khi nhận hàng (COD)";
        }
        if ("DA_THANH_TOAN".equals(status)) {
            return "Đã thanh toán";
        }
        if (normalizedNote.contains("VNPAY: DA THANH TOAN") || normalizedNote.contains("THANH TOAN: VNPAY")) {
            return "Đã thanh toán";
        }
        if ("THAT_BAI".equals(status)) {
            return "Thanh toán thất bại";
        }
        return "Chờ thanh toán";
    }

    private String resolveCustomerNote(DonHang_module order) {
        if (order == null || order.getGhi_chu() == null) {
            return "";
        }
        String note = order.getGhi_chu().trim();
        if (note.isBlank()) {
            return "";
        }
        String normalized = normalizeForMatch(note);
        if (normalized.startsWith("DON MUA HANG TU KHACH | THANH TOAN:")) {
            return "";
        }
        return note;
    }

    private String normalizeForMatch(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('Đ', 'D')
                .replace('đ', 'd');
        return normalized.toUpperCase(Locale.ROOT).trim();
    }

    @PostMapping("/hoanthanh")
    public String hoanThanhDonGiaoHang(
            @RequestParam("orderId") String orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication == null ? null : authentication.getName();
            donhangService.completeDeliveryOrder(orderId, username);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hoàn thành đơn hàng " + orderId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Không thể hoàn thành đơn hàng" : ex.getMessage());
        }
        return "redirect:/nhanvien/giaohang/donhang";
    }

    @PostMapping("/huy")
    public String huyDonGiaoHang(
            @RequestParam("orderId") String orderId,
            @RequestParam("reason") String reason,
            @RequestParam(value = "otherReason", required = false) String otherReason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication == null ? null : authentication.getName();
            String resolvedReason = resolveCancelReason(reason, otherReason);
            donhangService.cancelDeliveryOrder(orderId, resolvedReason, username);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng " + orderId);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() == null ? "Không thể hủy đơn hàng" : ex.getMessage());
        }
        return "redirect:/nhanvien/giaohang/donhang";
    }

    private String resolveCancelReason(String reason, String otherReason) {
        String selected = reason == null ? "" : reason.trim();
        if ("KHAC".equalsIgnoreCase(selected)) {
            String manual = otherReason == null ? "" : otherReason.trim();
            if (!manual.isBlank()) {
                return manual;
            }
        }
        return selected;
    }
}
