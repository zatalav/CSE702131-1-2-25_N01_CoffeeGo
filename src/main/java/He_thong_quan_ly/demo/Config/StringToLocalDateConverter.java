package He_thong_quan_ly.demo.Config;

import java.time.LocalDate;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import He_thong_quan_ly.demo.Util.VnDateParser;

@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate convert(String source) {
        if (source == null || source.trim().isBlank()) {
            return null;
        }
        return VnDateParser.parseRequired(source, "Ngày");
    }
}