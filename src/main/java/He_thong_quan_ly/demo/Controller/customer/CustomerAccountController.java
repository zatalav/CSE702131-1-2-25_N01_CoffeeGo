package He_thong_quan_ly.demo.Controller.customer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Config.CloudinaryViewConfigResolver;
import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlydonhangRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlykhachhangRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.DonHangDetailRepository;
import He_thong_quan_ly.demo.Util.VnDateParser;
import He_thong_quan_ly.demo.Util.VnIdentityValidator;

@Controller
@RequestMapping("/customer")
public class CustomerAccountController {

    private final QuanlykhachhangRepository khachhangRepository;
    private final QuanlydonhangRepository donhangRepository;
    private final DonHangDetailRepository donhangDetailRepository;
    private final CloudinaryViewConfigResolver cloudinaryViewConfigResolver;

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.upload-preset:coffee_upload}")
    private String cloudinaryUploadPreset;

    public CustomerAccountController(
            QuanlykhachhangRepository khachhangRepository,
            QuanlydonhangRepository donhangRepository,
            DonHangDetailRepository donhangDetailRepository,
            CloudinaryViewConfigResolver cloudinaryViewConfigResolver) {

        this.khachhangRepository = khachhangRepository;
        this.donhangRepository = donhangRepository;
        this.donhangDetailRepository = donhangDetailRepository;
        this.cloudinaryViewConfigResolver = cloudinaryViewConfigResolver;
    }

    @GetMapping("/profile")
    public String profile(@RequestParam("kh") String khachhangId, Model model) {
        var kh = khachhangRepository.findById(khachhangId).orElse(null);
        model.addAttribute("khachhang", kh);
        model.addAttribute("customerId", khachhangId);
        model.addAttribute("totalOrders", donhangRepository.countByKhachHangId(khachhangId));
        model.addAttribute("cloudinaryCloudName", cloudinaryViewConfigResolver.resolveCloudName(cloudinaryCloudName));
        model.addAttribute("cloudinaryUploadPreset", cloudinaryUploadPreset);
        return "Customer/Profile";
    }

    @GetMapping("/orders")
    public String orders(@RequestParam("kh") String khachhangId, Model model) {
        model.addAttribute("orders", donhangRepository.findByKhachHangIdOrderByNgayDatDesc(khachhangId));
        model.addAttribute("customerId", khachhangId);
        return "Customer/Orders";
    }

    @GetMapping("/orders/detail")
    public String orderDetail(
            @RequestParam("kh") String khachhangId,
            @RequestParam("id") String orderId,
            Model model) {
        DonHang_module order = donhangRepository.findById(orderId).orElse(null);
        if (order == null
                || order.getKhachHang() == null
                || order.getKhachHang().getKhachhang_id() == null
                || !order.getKhachHang().getKhachhang_id().equals(khachhangId)) {
            return "redirect:/customer/orders?kh=" + khachhangId;
        }

        var items = donhangDetailRepository.findByDonhangId(orderId);

        model.addAttribute("customerId", khachhangId);
        model.addAttribute("order", order);
        model.addAttribute("orderItems", items);
        return "Customer/OrderDetail";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("khachhangId") String khachhangId,
            @RequestParam(value = "Ten_KH", required = false) String tenKh,
            @RequestParam(value = "Gioi_tinh", required = false) String gioiTinh,
            @RequestParam(value = "Ngay_sinh", required = false) String ngaySinh,
            @RequestParam(value = "SDT", required = false) String sdt,
            @RequestParam(value = "Gmail", required = false) String gmail,
            @RequestParam(value = "Dia_chi", required = false) String diaChi,
            @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
            RedirectAttributes redirectAttributes) {
        var kh = khachhangRepository.findById(khachhangId).orElse(null);
        if (kh == null) {
            return "redirect:/customer/profile?kh=" + khachhangId;
        }
        try {
            kh.setTen_KH(tenKh == null ? null : tenKh.trim());
            kh.setGioi_tinh(gioiTinh);
            kh.setNgay_sinh(VnDateParser.parseOptional(ngaySinh, "Ngày sinh"));
            kh.setSDT(VnIdentityValidator.normalizeRequiredVietnamPhone(sdt));
            kh.setGmail(gmail == null ? null : gmail.trim());
            kh.setDia_chi(diaChi == null ? null : diaChi.trim());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customer/profile?kh=" + khachhangId;
        }

        if (avatarUrl != null && !avatarUrl.isBlank()) {
            kh.setImgKh(avatarUrl.trim());
        }
        khachhangRepository.save(kh);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công");
        return "redirect:/customer/profile?kh=" + khachhangId;
    }

}
