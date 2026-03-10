package He_thong_quan_ly.demo.Service.Admin.order;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;

@Component
public class OrderViewMapper {

    public NumberFormat vnNumberFormat() {
        return NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    }

    public String resolveName(DonHang_module dh) {
        if (dh.getNhanVien() != null && dh.getNhanVien().getTenNv() != null) {
            return dh.getNhanVien().getTenNv();
        }
        KhachHang_module kh = dh.getKhachHang();
        if (kh != null && kh.getTen_KH() != null) {
            return kh.getTen_KH();
        }
        return "";
    }

    public Map<String, String> toAdminRow(
            DonHang_module dh,
            DateTimeFormatter dateFormatter,
            DateTimeFormatter timeFormatter,
            NumberFormat numberFormat) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("code", dh.getDonhang_id());
        row.put("typeBadge", dh.getPhan_loai() == null ? "NV" : dh.getPhan_loai());
        row.put("name", resolveName(dh));
        row.put("total", formatTotal(numberFormat, dh.getTong_tien(), " đ"));
        putDateTime(row, dh.getNgay_dat(), dateFormatter, timeFormatter);

        String status = dh.getTrang_thai() == null ? "" : dh.getTrang_thai();
        row.put("status", status);
        row.put("statusValue", status);
        row.put("statusClass", resolveStatusClass(status));
        return row;
    }

    public String formatTotal(NumberFormat numberFormat, Long total, String suffix) {
        Long safeTotal = java.util.Optional.ofNullable(total).orElse(0L);
        return numberFormat.format(safeTotal) + suffix;
    }

    public void putDateTime(
            Map<String, String> row,
            LocalDateTime ngayDat,
            DateTimeFormatter dateFormatter,
            DateTimeFormatter timeFormatter) {
        row.put("date", ngayDat == null ? "" : ngayDat.format(dateFormatter));
        row.put("time", ngayDat == null ? "" : ngayDat.format(timeFormatter));
    }

    public void putCustomerPhone(Map<String, String> row, DonHang_module dh) {
        row.put("phone", dh.getKhachHang() == null || dh.getKhachHang().getSDT() == null
                ? ""
                : dh.getKhachHang().getSDT());
    }

    public void putCustomerContact(Map<String, String> row, DonHang_module dh) {
        putCustomerPhone(row, dh);
        row.put("address", dh.getKhachHang() == null || dh.getKhachHang().getDia_chi() == null
                ? ""
                : dh.getKhachHang().getDia_chi());
    }

    public String resolveStatusClass(String status) {
        String normalized = status == null ? "" : status.toLowerCase(Locale.ROOT);
        if (normalized.contains("thành") || normalized.contains("thanh")) {
            return "green";
        }
        if (normalized.contains("hủy") || normalized.contains("huy")) {
            return "red";
        }
        return "orange";
    }
}
