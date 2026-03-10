package He_thong_quan_ly.demo.Controller.NhanVien;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.KhoBepRepository;

@Controller
@RequestMapping("/nhanvien")
public class KhobepController {

    private final KhoBepRepository khoBepRepository;
    private final QuanlynhanvienRepository nhanvienRepository;
    private final QuanlynguyenlieuRepository nguyenLieuRepository;

    public KhobepController(
            KhoBepRepository khoBepRepository,
            QuanlynhanvienRepository nhanvienRepository,
            QuanlynguyenlieuRepository nguyenLieuRepository) {
        this.khoBepRepository = khoBepRepository;
        this.nhanvienRepository = nhanvienRepository;
        this.nguyenLieuRepository = nguyenLieuRepository;
    }

    @GetMapping("/khobep")
    @SuppressWarnings("boxing")
    public String khoBep(Model model, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        var nhanVien = nhanvienRepository.findByGmail(username)
                .or(() -> nhanvienRepository.findById(username))
                .orElse(null);
        String cosoId = nhanVien == null || nhanVien.getCoSo() == null ? null
                : nhanVien.getCoSo().getCosoId();

        var khoBepList = cosoId == null
                ? List.<He_thong_quan_ly.demo.Module.NhanVienKho.Khobep_module>of()
                : khoBepRepository.findAllByCoSo_CosoId(cosoId);
        Map<String, Double> slTonByNl = new HashMap<>();
        for (var kb : khoBepList) {
            if (kb.getNguyenLieu() != null && kb.getNguyenLieu().getNguyenlieuId() != null) {
                slTonByNl.put(kb.getNguyenLieu().getNguyenlieuId(), kb.getSlTon());
            }
        }

        List<Map<String, Object>> items = nguyenLieuRepository.findAllWithNhaCungCap().stream().map(nl -> {
            Map<String, Object> row = new HashMap<>();
            String nlId = nl == null ? "" : nl.getNguyenlieuId();
            Double slTonValue = nlId.isBlank() ? null : slTonByNl.getOrDefault(nlId, 0.0);
            double slTon = slTonValue == null ? 0.0 : slTonValue;
            String nhaCungCap = "";
            if (nl != null && nl.getDsCungCap() != null && !nl.getDsCungCap().isEmpty()) {
                nhaCungCap = nl.getDsCungCap().stream()
                        .map(cc -> cc.getNhaCungCap() == null ? "" : cc.getNhaCungCap().getTenNhaCungCap())
                        .filter(name -> name != null && !name.isBlank())
                        .distinct()
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
            }

            row.put("id", nlId);
            row.put("ten", nl == null ? "" : nl.getTenNguyenLieu());
            row.put("donVi", nl == null ? "" : nl.getDonVi());
            row.put("slTon", slTon);
            row.put("nhaCungCap", nhaCungCap);
            row.put("trangThai", computeTrangThai(slTon));
            return row;
        }).sorted(Comparator.comparing(o -> String.valueOf(o.get("ten")), String.CASE_INSENSITIVE_ORDER)).toList();

        model.addAttribute("items", items);
        return "NhanVien/Khobep";
    }

    private String computeTrangThai(Double slTon) {
        double value = slTon == null ? 0.0 : slTon;
        if (value <= 0.0) {
            return "Hết hàng";
        }
        if (value < 10.0) {
            return "Sắp hết hàng";
        }
        return "Còn hàng";
    }
}
