package He_thong_quan_ly.demo.Util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class VnDateParser {

    private static final DateTimeFormatter VI_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private VnDateParser() {
    }

    public static LocalDate parseOptional(String value, String fieldLabel) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return parseRequired(value, fieldLabel);
    }

    public static LocalDate parseRequired(String value, String fieldLabel) {
        String raw = value == null ? "" : value.trim();
        if (raw.isBlank()) {
            throw new IllegalArgumentException(fieldLabel + " không được để trống");
        }

        // Support compact Vietnamese date input like 22122005.
        if (raw.matches("\\d{8}")) {
            raw = raw.substring(0, 2) + "/" + raw.substring(2, 4) + "/" + raw.substring(4);
        }

        try {
            return LocalDate.parse(raw, VI_DATE);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException ignored) {
                throw new IllegalArgumentException(fieldLabel + " phải theo định dạng dd/MM/yyyy");
            }
        }
    }
}