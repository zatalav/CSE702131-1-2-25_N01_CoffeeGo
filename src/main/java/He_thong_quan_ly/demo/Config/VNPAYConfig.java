package He_thong_quan_ly.demo.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vnpay")
public class VNPAYConfig {

    private String tmnCode = "";
    private String hashSecret = "";
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:8080/customer/vnpay-return";

    public String getTmnCode() {
        return normalizeCredential(tmnCode);
    }

    public void setTmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }

    public String getHashSecret() {
        return normalizeCredential(hashSecret);
    }

    public void setHashSecret(String hashSecret) {
        this.hashSecret = hashSecret;
    }

    public String getPayUrl() {
        String value = safeTrim(payUrl);
        return value.isBlank() ? "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" : value;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getReturnUrl() {
        String value = safeTrim(returnUrl);
        return value.isBlank() ? "http://localhost:8080/customer/vnpay-return" : value;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeCredential(String value) {
        return safeTrim(value).replaceAll("\\s+", "");
    }
}
