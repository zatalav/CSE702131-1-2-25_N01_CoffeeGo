package He_thong_quan_ly.demo.auth.dto;

import java.time.Instant;

public class PendingRegisterOtp {

    private final String gmail;
    private final String password;
    private final String otp;
    private final Instant expiresAt;
    private final boolean verified;

    public PendingRegisterOtp(
            String gmail,
            String password,
            String otp,
            Instant expiresAt,
            boolean verified) {
        this.gmail = gmail;
        this.password = password;
        this.otp = otp;
        this.expiresAt = expiresAt;
        this.verified = verified;
    }

    public String gmail() {
        return gmail;
    }

    public String password() {
        return password;
    }

    public String otp() {
        return otp;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public boolean verified() {
        return verified;
    }

    public PendingRegisterOtp markVerified() {
        return new PendingRegisterOtp(gmail, password, otp, expiresAt, true);
    }
}

