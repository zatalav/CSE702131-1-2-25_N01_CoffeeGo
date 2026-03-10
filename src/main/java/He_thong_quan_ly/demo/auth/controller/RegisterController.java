package He_thong_quan_ly.demo.auth.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import He_thong_quan_ly.demo.auth.dto.PendingRegisterOtp;
import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlykhachhangRepository;
import He_thong_quan_ly.demo.auth.service.AuthSecurityService;
import He_thong_quan_ly.demo.auth.service.PendingRegisterCacheService;
import He_thong_quan_ly.demo.auth.util.EmailValidatorUtil;
import He_thong_quan_ly.demo.Util.VnDateParser;
import He_thong_quan_ly.demo.Util.VnIdentityValidator;

@Controller
public class RegisterController {

    private static final String SESSION_KEY = "PENDING_REGISTER_GMAIL";
    private static final long OTP_TTL_SECONDS = 300;
    private static final int OTP_LENGTH = 6;

    private final QuanlykhachhangRepository khachhangRepository;
    private final AuthSecurityService authSecurityService;
    private final PendingRegisterCacheService pendingRegisterCacheService;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(
            QuanlykhachhangRepository khachhangRepository,
            AuthSecurityService authSecurityService,
            PendingRegisterCacheService pendingRegisterCacheService,
            PasswordEncoder passwordEncoder) {
        this.khachhangRepository = khachhangRepository;
        this.authSecurityService = authSecurityService;
        this.pendingRegisterCacheService = pendingRegisterCacheService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/truycap/Dangky.html";
    }

    @PostMapping("/register/request-otp")
    @ResponseBody
    public ResponseEntity<?> requestOtp(
            @RequestBody Map<String, String> payload,
            jakarta.servlet.http.HttpSession session) {
        String gmail = safe(payload.get("gmail"));
        String normalizedGmail = EmailValidatorUtil.normalize(gmail);
        String password = safe(payload.get("password"));
        String confirmPassword = safe(payload.get("confirmPassword"));

        if (normalizedGmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"));
        }
        if (!EmailValidatorUtil.isValid(normalizedGmail)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email khong dung dinh dang"));
        }
        Optional<String> passwordError = authSecurityService.validatePassword(
                password,
                confirmPassword,
                6,
                "Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng",
                "Máº­t kháº©u tá»‘i thiá»ƒu 6 kÃ½ tá»±",
                "Máº­t kháº©u xÃ¡c nháº­n khÃ´ng khá»›p");
        if (passwordError.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", passwordError.get()));
        }
        if (khachhangRepository.existsByGmailIgnoreCase(normalizedGmail)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email Ä‘Ã£ tá»“n táº¡i"));
        }
        if (!authSecurityService.isMailConfigured()) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "ChÆ°a cáº¥u hÃ¬nh gá»­i email OTP"));
        }

        String otp = authSecurityService.generateNumericOtp(OTP_LENGTH);
        PendingRegisterOtp pending = new PendingRegisterOtp(
                normalizedGmail,
                passwordEncoder.encode(password),
                otp,
                java.time.Instant.now().plusSeconds(OTP_TTL_SECONDS),
                false);
        pendingRegisterCacheService.put(normalizedGmail, pending);
        session.setAttribute(SESSION_KEY, normalizedGmail);

        Optional<String> sendError = authSecurityService.sendOtpEmail(
                normalizedGmail,
                "CoffeeGo - MÃ£ xÃ¡c thá»±c OTP",
                "MÃ£ OTP cá»§a báº¡n lÃ : " + otp + "\nMÃ£ cÃ³ hiá»‡u lá»±c trong 5 phÃºt.",
                "Dá»‹ch vá»¥ gá»­i email chÆ°a sáºµn sÃ ng",
                "KhÃ´ng gá»­i Ä‘Æ°á»£c email OTP. Vui lÃ²ng kiá»ƒm tra cáº¥u hÃ¬nh SMTP.");
        if (sendError.isPresent()) {
            clearPending(session, normalizedGmail);
            return ResponseEntity.internalServerError().body(Map.of("message", sendError.get()));
        }

        return ResponseEntity.ok(Map.of("message", "ÄÃ£ gá»­i mÃ£ OTP vá» email"));
    }

    @PostMapping("/register/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(
            @RequestBody Map<String, String> payload,
            jakarta.servlet.http.HttpSession session) {
        String otp = safe(payload.get("otp"));
        PendingRegisterOtp pending = getPending(session);
        if (pending == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "OTP khÃ´ng tá»“n táº¡i hoáº·c Ä‘Ã£ háº¿t háº¡n"));
        }
        if (authSecurityService.isOtpExpired(pending.expiresAt())) {
            clearPending(session, pending.gmail());
            return ResponseEntity.badRequest().body(Map.of("message", "OTP Ä‘Ã£ háº¿t háº¡n"));
        }
        if (!pending.otp().equals(otp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP khÃ´ng Ä‘Ãºng"));
        }

        pendingRegisterCacheService.put(pending.gmail(), pending.markVerified());
        return ResponseEntity.ok(Map.of("message", "XÃ¡c thá»±c OTP thÃ nh cÃ´ng"));
    }

    @PostMapping("/register/complete")
    @ResponseBody
    public ResponseEntity<?> completeRegister(
            @RequestBody Map<String, String> payload,
            jakarta.servlet.http.HttpSession session) {
        PendingRegisterOtp pending = getPending(session);
        if (pending == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lÃ²ng xÃ¡c thá»±c OTP trÆ°á»›c"));
        }
        if (authSecurityService.isOtpExpired(pending.expiresAt())) {
            clearPending(session, pending.gmail());
            return ResponseEntity.badRequest().body(Map.of("message", "OTP Ä‘Ã£ háº¿t háº¡n"));
        }

        String otp = safe(payload.get("otp"));
        boolean otpAccepted = pending.verified() || (!otp.isBlank() && pending.otp().equals(otp));
        if (!otpAccepted) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lÃ²ng xÃ¡c thá»±c OTP trÆ°á»›c"));
        }

        String tenKh = safe(payload.get("tenKh"));
        String sdt = safe(payload.get("sdt"));
        String gioiTinh = safe(payload.get("gioiTinh"));
        String ngaySinhText = safe(payload.get("ngaySinh"));
        String diaChi = safe(payload.get("diaChi"));

        Optional<KhachHang_module> existing = khachhangRepository.findByLogin(pending.gmail());
        if (existing.isPresent()) {
            clearPending(session, pending.gmail());
            return ResponseEntity.ok(Map.of(
                    "redirect", "/customer/menu?registered=1&kh=" + existing.get().getKhachhang_id()));
        }

        if (tenKh.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Há» tÃªn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"));
        }
        if (sdt.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"));
        }
        final String normalizedSdt;
        try {
            normalizedSdt = VnIdentityValidator.normalizeRequiredVietnamPhone(sdt);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
        if (khachhangRepository.existsBySDT(normalizedSdt)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i"));
        }

        LocalDate ngaySinh = VnDateParser.parseOptional(ngaySinhText, "NgÃ y sinh");

        KhachHang_module kh = new KhachHang_module();
        kh.setKhachhang_id(generateKhachHangId());
        kh.setTen_KH(tenKh);
        kh.setGmail(pending.gmail());
        kh.setSDT(normalizedSdt);
        kh.setPassword(pending.password());
        kh.setGioi_tinh(gioiTinh.isBlank() ? "KhÃ¡c" : gioiTinh);
        kh.setNgay_sinh(ngaySinh);
        kh.setDia_chi(diaChi);
        kh.setTong_so_DH_mua(0);
        kh.setTrang_thai("HOAT_DONG");

        khachhangRepository.save(kh);
        clearPending(session, pending.gmail());

        return ResponseEntity.ok(Map.of(
                "redirect", "/customer/menu?registered=1&kh=" + kh.getKhachhang_id()));
    }

    @PostMapping("/register")
    public String registerCustomer(
            @RequestParam("tenKh") String tenKh,
            @RequestParam("gmail") String gmail,
            @RequestParam("sdt") String sdt,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("gioiTinh") String gioiTinh,
            @RequestParam("ngaySinh") String ngaySinh,
            @RequestParam("diaChi") String diaChi) {

        String normalizedGmail = EmailValidatorUtil.normalize(gmail);
        if (normalizedGmail.isBlank()) {
            return redirectError("Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        if (!EmailValidatorUtil.isValid(normalizedGmail)) {
            return redirectError("Email khong dung dinh dang");
        }
        if (sdt == null || sdt.isBlank()) {
            return redirectError("Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        final String normalizedSdt;
        try {
            normalizedSdt = VnIdentityValidator.normalizeRequiredVietnamPhone(sdt);
        } catch (IllegalArgumentException ex) {
            return redirectError(ex.getMessage());
        }
        Optional<String> passwordError = authSecurityService.validatePassword(
                password,
                confirmPassword,
                1,
                "Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng",
                "",
                "Máº­t kháº©u xÃ¡c nháº­n khÃ´ng khá»›p");
        if (passwordError.isPresent()) {
            return redirectError(passwordError.get());
        }
        if (khachhangRepository.existsByGmailIgnoreCase(normalizedGmail)) {
            return redirectError("Email Ä‘Ã£ tá»“n táº¡i");
        }
        if (khachhangRepository.existsBySDT(normalizedSdt)) {
            return redirectError("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i");
        }

        KhachHang_module kh = new KhachHang_module();
        kh.setKhachhang_id(generateKhachHangId());
        kh.setTen_KH(tenKh);
        kh.setGmail(normalizedGmail);
        kh.setSDT(normalizedSdt);
        kh.setPassword(passwordEncoder.encode(password));
        kh.setGioi_tinh(gioiTinh);
        kh.setNgay_sinh(VnDateParser.parseOptional(ngaySinh, "NgÃ y sinh"));
        kh.setDia_chi(diaChi);
        kh.setTong_so_DH_mua(0);
        kh.setTrang_thai("HOAT_DONG");

        khachhangRepository.save(kh);
        return "redirect:/customer/menu?registered=1&kh=" + kh.getKhachhang_id();
    }

    private String generateKhachHangId() {
        List<String> ids = khachhangRepository.findTopKhachHangId(PageRequest.of(0, 1));
        if (ids == null || ids.isEmpty() || ids.get(0) == null) {
            return "KH001";
        }
        String last = ids.get(0).trim();
        String digits = last.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return "KH001";
        }
        int number = Integer.parseInt(digits);
        return String.format("KH%03d", number + 1);
    }

    private String redirectError(String message) {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "redirect:/register?error=" + encoded;
    }

    private String safe(String value) {
        return authSecurityService.safe(value);
    }

    private PendingRegisterOtp getPending(jakarta.servlet.http.HttpSession session) {
        Object data = session.getAttribute(SESSION_KEY);
        if (!(data instanceof String gmailKey) || gmailKey.isBlank()) {
            return null;
        }
        return pendingRegisterCacheService.get(gmailKey);
    }

    private void clearPending(jakarta.servlet.http.HttpSession session, String gmail) {
        pendingRegisterCacheService.remove(gmail);
        session.removeAttribute(SESSION_KEY);
    }
}
