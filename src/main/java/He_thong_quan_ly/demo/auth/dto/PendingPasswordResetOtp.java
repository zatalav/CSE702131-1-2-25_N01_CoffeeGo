package He_thong_quan_ly.demo.auth.dto;

import java.time.Instant;

public class PendingPasswordResetOtp {

    private final String gmail;
    private final String otp;
    private final Instant expiresAt;
    private final boolean verified;

    public PendingPasswordResetOtp(String gmail, String otp, Instant expiresAt, boolean verified) {
        this.gmail = gmail;
        this.otp = otp;
        this.expiresAt = expiresAt;
        this.verified = verified;
    }

    public String gmail() {
        return gmail;
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

    public PendingPasswordResetOtp markVerified() {
        return new PendingPasswordResetOtp(gmail, otp, expiresAt, true);
    }
}

