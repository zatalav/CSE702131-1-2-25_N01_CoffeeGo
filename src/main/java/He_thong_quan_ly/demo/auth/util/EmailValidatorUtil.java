package He_thong_quan_ly.demo.auth.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class EmailValidatorUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$");

    private EmailValidatorUtil() {
    }

    public static String normalize(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValid(String email) {
        String normalized = normalize(email);
        if (normalized.isEmpty() || normalized.length() > 254) {
            return false;
        }
        return EMAIL_PATTERN.matcher(normalized).matches();
    }
}
