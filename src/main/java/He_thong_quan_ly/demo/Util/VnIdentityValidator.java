package He_thong_quan_ly.demo.Util;

import java.util.regex.Pattern;

public final class VnIdentityValidator {

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern VIETNAM_PHONE_PATTERN = Pattern
            .compile("^(03[2-9]|05[2689]|07[06789]|08[1-9]|09[0-9])\\d{7}$");

    private VnIdentityValidator() {
    }

    public static String normalizeCccdOrNull(String cccd) {
        String digits = digitsOnly(cccd);
        if (digits.isBlank()) {
            return null;
        }
        if (!CCCD_PATTERN.matcher(digits).matches()) {
            throw new IllegalArgumentException("CCCD phải gồm đúng 12 chữ số");
        }
        return digits;
    }

    public static String normalizeVietnamPhoneOrNull(String phone) {
        String normalized = normalizePhone(phone);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        if (!VIETNAM_PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng di động Việt Nam");
        }
        return normalized;
    }

    public static String normalizeRequiredVietnamPhone(String phone) {
        String normalized = normalizeVietnamPhoneOrNull(phone);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }
        return normalized;
    }

    private static String normalizePhone(String value) {
        String digits = digitsOnly(value);
        if (digits.isBlank()) {
            return null;
        }
        if (digits.startsWith("84") && digits.length() == 11) {
            return "0" + digits.substring(2);
        }
        return digits;
    }

    private static String digitsOnly(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }
}