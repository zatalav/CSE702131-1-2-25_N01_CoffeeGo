package He_thong_quan_ly.demo.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/truycap/Dangnhap.html";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forward:/truycap/Quenmatkhau.html";
    }

    @GetMapping("/forgot-password/otp")
    public String forgotPasswordOtpPage() {
        return "forward:/truycap/QuenmatkhauOtp.html";
    }

    @GetMapping("/forgot-password/reset")
    public String forgotPasswordResetPage() {
        return "forward:/truycap/QuenmatkhauReset.html";
    }
}

