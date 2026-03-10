package He_thong_quan_ly.demo.Util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

public final class PdfFontUtil {

    private static BaseFont unicodeBaseFont;

    private PdfFontUtil() {
    }

    public static Font titleFont(float size) {
        return new Font(resolveBaseFont(), size, Font.BOLD);
    }

    public static Font headerFont(float size) {
        return new Font(resolveBaseFont(), size, Font.BOLD);
    }

    public static Font normalFont(float size) {
        return new Font(resolveBaseFont(), size, Font.NORMAL);
    }

    private static synchronized BaseFont resolveBaseFont() {
        if (unicodeBaseFont != null) {
            return unicodeBaseFont;
        }

        List<String> candidates = List.of(
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/tahoma.ttf",
                "C:/Windows/Fonts/times.ttf");

        for (String fontPath : candidates) {
            try {
                Path path = Paths.get(fontPath);
                if (Files.exists(path)) {
                    unicodeBaseFont = BaseFont.createFont(
                            path.toString(),
                            BaseFont.IDENTITY_H,
                            BaseFont.EMBEDDED);
                    return unicodeBaseFont;
                }
            } catch (DocumentException | java.io.IOException ignored) {
            }
        }

        try {
            unicodeBaseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            return unicodeBaseFont;
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("Không thể khởi tạo font PDF", e);
        }
    }
}
