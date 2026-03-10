package He_thong_quan_ly.demo.auth.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;
import He_thong_quan_ly.demo.auth.dto.PendingPasswordResetOtp;
import He_thong_quan_ly.demo.Repository.Admin.QuanlykhachhangRepository;
import He_thong_quan_ly.demo.auth.service.AuthSecurityService;
import He_thong_quan_ly.demo.auth.service.PendingPasswordResetCacheService;
import He_thong_quan_ly.demo.auth.util.EmailValidatorUtil;

@RestController
public class ForgotPasswordController {

    private static final String SESSION_KEY = "PENDING_RESET_GMAIL";
    private static final long OTP_TTL_SECONDS = 300;
    private static final int OTP_LENGTH = 6;

    private final QuanlykhachhangRepository khachhangRepository;
    private final AuthSecurityService authSecurityService;
    private final PendingPasswordResetCacheService pendingPasswordResetCacheService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(
            QuanlykhachhangRepository khachhangRepository,
            AuthSecurityService authSecurityService,
            PendingPasswordResetCacheService pendingPasswordResetCacheService,
            PasswordEncoder passwordEncoder) {
        this.khachhangRepository = khachhangRepository;
        this.authSecurityService = authSecurityService;
        this.pendingPasswordResetCacheService = pendingPasswordResetCacheService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password/request-otp")
    @ResponseBody
    public ResponseEntity<?> requestOtp(
            @RequestBody Map<String, String> payload,
            jakarta.servlet.http.HttpSession session) {
        String gmail = safe(payload.get("gmail"));
        String normalizedGmail = EmailValidatorUtil.normalize(gmail);

        if (!EmailValidatorUtil.isValid(normalizedGmail)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email format is invalid"));
        }
        if (!khachhangRepository.existsByGmailIgnoreCase(normalizedGmail)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email does not exist"));
        }
        if (!authSecurityService.isMailConfigured()) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Mail service is not configured"));
        }

        String otp = authSecurityService.generateNumericOtp(OTP_LENGTH);
        String key = pendingPasswordResetCacheService.normalizeKey(normalizedGmail);
        PendingPasswordResetOtp pending = new PendingPasswordResetOtp(
                normalizedGmail,
                otp,
                java.time.Instant.now().plusSeconds(OTP_TTL_SECONDS),
                false);

        pendingPasswordResetCacheService.put(key, pending);
        session.setAttribute(SESSION_KEY, key);

        Optional<String> sendError = authSecurityService.sendOtpEmail(
                normalizedGmail,
                "CoffeeGo - Password Reset OTP",
                "Your OTP code is: " + otp + "\nThis OTP expires in 5 minutes.",
                "Mail service is unavailable",
                "Cannot send OTP email. Please check SMTP settings.");
        if (sendError.isPresent()) {
            clearPending(session, key);
            return ResponseEntity.internalServerError().body(Map.of("message", sendError.get()));
        }

        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/forgot-password/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(
            @RequestBody Map<String, String> payload,
            jakarta.servlet.http.HttpSession session) {
        String otp = safe(payload.get("otp"));
        PendingPasswordResetOtp pending = getPending(session);

        if (pending == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP is missing or expired"));
        }
        if (authSecurityService.isOtpExpired(pending.expiresAt())) {
            clearPending(session, pending.gmail());
            return ResponseEntity.badRequest().body(Map.of("message", "OTP is expired"));
        }
        if (!pending.otp().equals(otp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP is invalid"));
        }

        pendingPasswordResetCacheService.put(pending.gmail(), pending.markVerified());
        return ResponseEntity.ok(Map.of("message", "OTP verified"));
    }

    @PostMapping("/forgot-password/reset")
    @ResponseBody
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> payload,
            jakarta.servlet.http.HttpSession session) {
        PendingPasswordResetOtp pending = getPending(session);
        if (pending == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please verify OTP first"));
        }
        if (authSecurityService.isOtpExpired(pending.expiresAt())) {
            clearPending(session, pending.gmail());
            return ResponseEntity.badRequest().body(Map.of("message", "OTP is expired"));
        }

        String otp = safe(payload.get("otp"));
        boolean otpAccepted = pending.verified() || (!otp.isBlank() && pending.otp().equals(otp));
        if (!otpAccepted) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please verify OTP first"));
        }

        String newPassword = safe(payload.get("newPassword"));
        String confirmPassword = safe(payload.get("confirmPassword"));

        Optional<String> passwordError = authSecurityService.validatePassword(
                newPassword,
                confirmPassword,
                6,
                "New password is required",
                "Password must be at least 6 characters",
                "Password confirmation does not match");
        if (passwordError.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", passwordError.get()));
        }

        Optional<KhachHang_module> khOpt = khachhangRepository.findByLogin(pending.gmail());
        if (khOpt.isEmpty()) {
            clearPending(session, pending.gmail());
            return ResponseEntity.badRequest().body(Map.of("message", "Account not found"));
        }

        KhachHang_module khachHang = khOpt.get();
        khachHang.setPassword(passwordEncoder.encode(newPassword));
        khachhangRepository.save(khachHang);

        clearPending(session, pending.gmail());
        return ResponseEntity.ok(Map.of("message", "Password reset successful", "redirect", "/login?reset=1"));
    }

    private PendingPasswordResetOtp getPending(jakarta.servlet.http.HttpSession session) {
        Object data = session.getAttribute(SESSION_KEY);
        if (!(data instanceof String gmailKey) || gmailKey.isBlank()) {
            return null;
        }
        return pendingPasswordResetCacheService.get(gmailKey);
    }

    private void clearPending(jakarta.servlet.http.HttpSession session, String gmail) {
        pendingPasswordResetCacheService.remove(gmail);
        session.removeAttribute(SESSION_KEY);
    }

    private String safe(String value) {
        return authSecurityService.safe(value);
    }
}
