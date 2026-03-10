package He_thong_quan_ly.demo.Service.Admin.order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import He_thong_quan_ly.demo.Module.DTO.OrderCreateRequest;
import He_thong_quan_ly.demo.Module.DTO.OrderItemRequest;
import He_thong_quan_ly.demo.Module.Admin.CoSo_module;
import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.NhanVienKho.Khobep_module;
import He_thong_quan_ly.demo.Module.bang_phu.CongThuc;
import He_thong_quan_ly.demo.Module.bang_phu.DonHang_detail;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.KhoBepRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.CongThucRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.DonHangDetailRepository;

@Service
public class OrderInventoryService {

    private final CongThucRepository congThucRepo;
    private final DonHangDetailRepository donhangDetailRepo;
    private final QuanlynguyenlieuRepository nguyenLieuRepo;
    private final KhoBepRepository khoBepRepository;

    public OrderInventoryService(
            CongThucRepository congThucRepo,
            DonHangDetailRepository donhangDetailRepo,
            QuanlynguyenlieuRepository nguyenLieuRepo,
            KhoBepRepository khoBepRepository) {
        this.congThucRepo = congThucRepo;
        this.donhangDetailRepo = donhangDetailRepo;
        this.nguyenLieuRepo = nguyenLieuRepo;
        this.khoBepRepository = khoBepRepository;
    }

    public Map<String, Double> buildRequiredNguyenLieu(String cosoId, OrderCreateRequest request) {
        Map<String, Double> required = new LinkedHashMap<>();
        if (request.getItems() == null) {
            return required;
        }

        for (OrderItemRequest item : request.getItems()) {
            if (item.getId() == null || item.getId().isBlank()) {
                continue;
            }
            int qty = Math.max(1, item.getQty());
            List<CongThuc> congThucs = congThucRepo.findBySanPhamId(item.getId());
            double sizeFactor = resolveSizeFactor(item.getSize());
            for (CongThuc ct : congThucs) {
                if (ct.getNguyenLieuId() == null) {
                    continue;
                }
                Double slValue = ct.getSoLuong();
                double sl = slValue == null ? 0.0 : slValue;
                if (sl <= 0) {
                    continue;
                }
                String nlId = ct.getNguyenLieuId();
                double add = sl * qty * sizeFactor;
                double converted = convertToKhoUnit(nlId, add, ct.getDonVi(), cosoId);
                required.put(nlId, required.getOrDefault(nlId, 0.0) + converted);
            }
        }
        return required;
    }

    public Map<String, Double> buildRequiredNguyenLieuFromDetails(String cosoId, String donhangId) {
        Map<String, Double> required = new LinkedHashMap<>();
        List<DonHang_detail> details = donhangDetailRepo.findByDonhangId(donhangId);
        if (details == null || details.isEmpty()) {
            return required;
        }

        for (DonHang_detail detail : details) {
            if (detail.getSanPham() == null || detail.getSanPham().getSanPhamId() == null) {
                continue;
            }
            int qty = Math.max(1, detail.getSL());
            List<CongThuc> congThucs = congThucRepo.findBySanPhamId(detail.getSanPham().getSanPhamId());
            double sizeFactor = resolveSizeFactor(detail.getSize());
            for (CongThuc ct : congThucs) {
                if (ct.getNguyenLieuId() == null) {
                    continue;
                }
                Double slValue = ct.getSoLuong();
                double sl = slValue == null ? 0.0 : slValue;
                if (sl <= 0) {
                    continue;
                }
                String nlId = ct.getNguyenLieuId();
                double add = sl * qty * sizeFactor;
                double converted = convertToKhoUnit(nlId, add, ct.getDonVi(), cosoId);
                required.put(nlId, required.getOrDefault(nlId, 0.0) + converted);
            }
        }
        return required;
    }

    public void validateNguyenLieuStock(String cosoId, Map<String, Double> requiredByNguyenLieu) {
        for (Map.Entry<String, Double> entry : requiredByNguyenLieu.entrySet()) {
            String nlId = entry.getKey();
            double required = entry.getValue();
            Khobep_module khoBep = khoBepRepository
                    .findByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(cosoId, nlId)
                    .orElse(null);
            double slTon = khoBep == null ? 0.0 : khoBep.getSlTon();
            if (slTon < required) {
                String ten = nguyenLieuRepo.findById(nlId)
                        .map(NguyenLieu_module::getTenNguyenLieu)
                        .orElse("nguyen lieu");
                throw new RuntimeException("Het nguyen lieu (kho bep): " + ten);
            }
        }
    }

    public void deductNguyenLieuStock(String cosoId, Map<String, Double> requiredByNguyenLieu) {
        for (Map.Entry<String, Double> entry : requiredByNguyenLieu.entrySet()) {
            String nlId = entry.getKey();
            double required = entry.getValue();
            Khobep_module khoBep = khoBepRepository
                    .findByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(cosoId, nlId)
                    .orElseThrow(() -> new RuntimeException("Khong tim thay nguyen lieu trong kho bep"));
            double newSl = Math.max(0.0, khoBep.getSlTon() - required);
            khoBep.setSlTon(newSl);
            khoBepRepository.save(khoBep);
        }
    }

    public void addBackNguyenLieuStock(String cosoId, CoSo_module coSo, Map<String, Double> requiredByNguyenLieu) {
        for (Map.Entry<String, Double> entry : requiredByNguyenLieu.entrySet()) {
            String nlId = entry.getKey();
            double required = entry.getValue();
            Khobep_module khoBep = khoBepRepository
                    .findByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(cosoId, nlId)
                    .orElseGet(() -> {
                        Khobep_module kb = new Khobep_module();
                        kb.setCoSo(coSo);
                        kb.setNguyenlieuId(nlId);
                        NguyenLieu_module nl = nguyenLieuRepo.findById(nlId).orElse(null);
                        kb.setNguyenLieu(nl);
                        kb.setDonVi(nl != null ? nl.getDonVi() : null);
                        kb.setSlTon(0);
                        return kb;
                    });
            if (khoBep.getCoSo() == null) {
                khoBep.setCoSo(coSo);
            }
            if (khoBep.getNguyenlieuId() == null || khoBep.getNguyenlieuId().isBlank()) {
                khoBep.setNguyenlieuId(nlId);
            }
            if (khoBep.getNguyenLieu() == null) {
                khoBep.setNguyenLieu(nguyenLieuRepo.findById(nlId).orElse(null));
            }
            double newSl = khoBep.getSlTon() + required;
            khoBep.setSlTon(newSl);
            if (khoBep.getDonVi() == null || khoBep.getDonVi().isBlank()) {
                String donVi = nguyenLieuRepo.findById(nlId)
                        .map(NguyenLieu_module::getDonVi)
                        .orElse(null);
                khoBep.setDonVi(donVi);
            }
            khoBepRepository.save(khoBep);
        }
    }

    private double resolveSizeFactor(String size) {
        if (size == null || size.isBlank()) {
            return 1.0;
        }
        String normalized = size.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "S" -> 0.8;
            case "M" -> 1.0;
            case "L" -> 1.2;
            case "XL" -> 1.4;
            default -> 1.0;
        };
    }

    private double convertToKhoUnit(String nguyenLieuId, double amount, String recipeUnit, String cosoId) {
        String khoUnit = resolveKhoUnit(nguyenLieuId, cosoId);
        return convertUnit(amount, recipeUnit, khoUnit);
    }

    private String resolveKhoUnit(String nguyenLieuId, String cosoId) {
        if (nguyenLieuId == null || nguyenLieuId.isBlank()) {
            return null;
        }
        if (cosoId != null && !cosoId.isBlank()) {
            Khobep_module khoBep = khoBepRepository
                    .findByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(cosoId, nguyenLieuId)
                    .orElse(null);
            if (khoBep != null && khoBep.getDonVi() != null && !khoBep.getDonVi().isBlank()) {
                return khoBep.getDonVi();
            }
        }
        return nguyenLieuRepo.findById(nguyenLieuId)
                .map(NguyenLieu_module::getDonVi)
                .orElse(null);
    }

    private double convertUnit(double value, String fromUnit, String toUnit) {
        String from = normalizeUnit(fromUnit);
        String to = normalizeUnit(toUnit);
        if (from == null || to == null || from.equals(to)) {
            return value;
        }

        if ((from.equals("g") || from.equals("gram")) && to.equals("kg")) {
            return value / 1000.0;
        }
        if (from.equals("kg") && (to.equals("g") || to.equals("gram"))) {
            return value * 1000.0;
        }
        if ((from.equals("ml") || from.equals("milliliter"))
                && (to.equals("l") || to.equals("lit") || to.equals("liter"))) {
            return value / 1000.0;
        }
        if ((from.equals("l") || from.equals("lit") || from.equals("liter"))
                && (to.equals("ml") || to.equals("milliliter"))) {
            return value * 1000.0;
        }

        return value;
    }

    private String normalizeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return null;
        }
        String normalized = unit.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "kilogram", "kg" -> "kg";
            case "gram", "g" -> "gram";
            case "ml", "milliliter", "mililiter" -> "ml";
            case "l", "lit", "liter" -> "l";
            default -> normalized;
        };
    }
}
