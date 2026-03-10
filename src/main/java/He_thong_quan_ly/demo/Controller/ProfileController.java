package He_thong_quan_ly.demo.Controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Config.CloudinaryViewConfigResolver;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Util.VnDateParser;
import He_thong_quan_ly.demo.Util.VnIdentityValidator;

@Controller
public class ProfileController {

    private final QuanlynhanvienRepository nhanvienRepository;
    private final CloudinaryViewConfigResolver cloudinaryViewConfigResolver;

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.upload-preset:coffee_upload}")
    private String cloudinaryUploadPreset;

    public ProfileController(
            QuanlynhanvienRepository nhanvienRepository,
            CloudinaryViewConfigResolver cloudinaryViewConfigResolver) {
        this.nhanvienRepository = nhanvienRepository;
        this.cloudinaryViewConfigResolver = cloudinaryViewConfigResolver;
    }

    @GetMapping("/profile")
    public String profile(
            Model model,
            Authentication authentication,
            @RequestParam(value = "id", required = false) String nhanvienId) {

        final String username = authentication != null && authentication.isAuthenticated()
                ? authentication.getName()
                : null;

        if (username == null) {
            return "redirect:/login";
        }

        var authNhanVien = nhanvienRepository.findByGmail(username)
                .or(() -> nhanvienRepository.findById(username))
                .orElse(null);

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");

        boolean allowIdView = isAdmin ||
                (authNhanVien != null &&
                        nhanvienId != null &&
                        (nhanvienId.equalsIgnoreCase(authNhanVien.getNhanvienId())
                                || nhanvienId.equalsIgnoreCase(authNhanVien.getGmail())));

        var nhanVien = allowIdView && nhanvienId != null && !nhanvienId.isBlank()
                ? nhanvienRepository.findById(nhanvienId).orElse(authNhanVien)
                : authNhanVien;

        boolean isPhucVu = false;
        String coSoName = "";

        if (nhanVien != null && nhanVien.getChucVu() != null) {

            String chucVu = nhanVien.getChucVu().toLowerCase(Locale.ROOT);

            isPhucVu = chucVu.contains("phục vụ") || chucVu.contains("phuc vu");

            if (isPhucVu && nhanVien.getCoSo() != null) {
                coSoName = nhanVien.getCoSo().getTenCs();
            }
        }

        model.addAttribute("nhanvien", nhanVien);
        model.addAttribute("isPhucVu", isPhucVu);
        model.addAttribute("coSoName", coSoName);
        model.addAttribute("cloudinaryCloudName", cloudinaryViewConfigResolver.resolveCloudName(cloudinaryCloudName));
        model.addAttribute("cloudinaryUploadPreset", cloudinaryUploadPreset);

        return "Trangcanhan";
    }

    @PostMapping("/profile/update")
    public String updateProfile(

            Authentication authentication,

            @RequestParam(value = "tenNv", required = false) String tenNv,
            @RequestParam(value = "gioiTinh", required = false) String gioiTinh,

            @RequestParam(value = "ngaySinh", required = false) String ngaySinh,

            @RequestParam(value = "cccd", required = false) String cccd,
            @RequestParam(value = "sdt", required = false) String sdt,
            @RequestParam(value = "diaChi", required = false) String diaChi,
            @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
            RedirectAttributes redirectAttributes) {

        final String username = authentication != null && authentication.isAuthenticated()
                ? authentication.getName()
                : null;

        if (username == null) {
            return "redirect:/login";
        }

        var nhanVien = nhanvienRepository.findByGmail(username)
                .or(() -> nhanvienRepository.findById(username))
                .orElse(null);

        if (nhanVien == null) {
            return "redirect:/profile";
        }

        if (tenNv != null) {
            nhanVien.setTenNv(tenNv.trim());
        }

        try {
            nhanVien.setGioiTinh(gioiTinh);
            nhanVien.setNgaySinh(VnDateParser.parseOptional(ngaySinh, "Ngày sinh"));
            nhanVien.setCccd(VnIdentityValidator.normalizeCccdOrNull(cccd));
            nhanVien.setSdt(VnIdentityValidator.normalizeVietnamPhoneOrNull(sdt));
            nhanVien.setDiaChi(diaChi == null ? null : diaChi.trim());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/profile";
        }

        if (avatarUrl != null && !avatarUrl.isBlank()) {
            nhanVien.setImgNv(avatarUrl.trim());
        }

        nhanvienRepository.save(nhanVien);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công");

        return "redirect:/profile";
    }

    private boolean hasRole(Authentication authentication, String role) {

        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }

}
