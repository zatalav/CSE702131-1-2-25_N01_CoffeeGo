package He_thong_quan_ly.demo.Service.customer.checkout;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import He_thong_quan_ly.demo.Config.VNPAYConfig;
import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class CheckoutVnpayService {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutVnpayService.class);

    private final VNPAYConfig vnpayConfig;

    public CheckoutVnpayService(VNPAYConfig vnpayConfig) {
        this.vnpayConfig = vnpayConfig;
    }

    public boolean isConfigured() {
        return !(vnpayConfig.getTmnCode().isBlank() || vnpayConfig.getHashSecret().isBlank());
    }

    public String buildPaymentUrl(DonHang_module donhang, HttpServletRequest httpRequest) {
        String tmnCode = vnpayConfig.getTmnCode();
        String hashSecret = vnpayConfig.getHashSecret();
        String payUrl = vnpayConfig.getPayUrl();
        String returnUrl = vnpayConfig.getReturnUrl();

        long amount = Math.max(0L, donhang.getTong_tien()) * 100L;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", donhang.getDonhang_id());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + donhang.getDonhang_id());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", resolveClientIp(httpRequest));
        params.put("vnp_CreateDate", now.format(formatter));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        String hashData = buildVnpHashData(params);
        String queryData = buildVnpQueryData(params);
        String secureHash = hmacSha512(hashSecret, hashData);
        String query = queryData + "&vnp_SecureHashType=HmacSHA512&vnp_SecureHash=" + secureHash;
        logger.info("VNPAY request prepared: tmnCode={}, returnUrl={}, txnRef={}, amount={}, ip={}",
                tmnCode,
                returnUrl,
                donhang.getDonhang_id(),
                amount,
                params.get("vnp_IpAddr"));
        return payUrl + "?" + query;
    }

    public boolean verifySignature(Map<String, String> inputParams) {
        String hashSecret = vnpayConfig.getHashSecret();
        if (hashSecret.isBlank() || inputParams == null) {
            return false;
        }
        String receivedHash = inputParams.getOrDefault("vnp_SecureHash", "");
        if (receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : inputParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isBlank() || value == null || value.isBlank()) {
                continue;
            }
            if ("vnp_SecureHash".equalsIgnoreCase(key) || "vnp_SecureHashType".equalsIgnoreCase(key)) {
                continue;
            }
            sorted.put(key, value);
        }

        String hashData = buildVnpHashData(sorted);
        String calculated = hmacSha512(hashSecret, hashData);
        return calculated.equalsIgnoreCase(receivedHash);
    }

    public String encodeQueryParam(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String buildVnpHashData(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + encodeQueryParam(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String buildVnpQueryData(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> encodeQueryParam(e.getKey()) + "=" + encodeQueryParam(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("Khong the ky du lieu VNPAY");
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] split = forwarded.split(",");
            if (split.length > 0 && !split[0].isBlank()) {
                return split[0].trim();
            }
        }
        String ip = request.getRemoteAddr();
        if (ip == null || ip.isBlank()) {
            return "127.0.0.1";
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
