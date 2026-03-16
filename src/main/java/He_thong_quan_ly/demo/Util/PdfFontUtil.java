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
                "C:/Windows/Fonts/arialuni.ttf",
                "C:/Windows/Fonts/tahoma.ttf",
                "C:/Windows/Fonts/times.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf");

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

        throw new IllegalStateException(
                "Khong tim thay font Unicode de tao PDF. Can cai dat font he thong ho tro tieng Viet.");
    }
}
