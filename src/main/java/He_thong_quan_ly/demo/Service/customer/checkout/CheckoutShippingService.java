package He_thong_quan_ly.demo.Service.customer.checkout;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import He_thong_quan_ly.demo.Module.Admin.CoSo_module;
import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlycosoRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;

@Service
public class CheckoutShippingService {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutShippingService.class);

    private final QuanlycosoRepository cosoRepository;
    private final QuanlynhanvienRepository nhanvienRepository;
    private final ShippingDistanceResolver shippingDistanceResolver;

    public CheckoutShippingService(
            QuanlycosoRepository cosoRepository,
            QuanlynhanvienRepository nhanvienRepository,
            ShippingDistanceResolver shippingDistanceResolver) {
        this.cosoRepository = cosoRepository;
        this.nhanvienRepository = nhanvienRepository;
        this.shippingDistanceResolver = shippingDistanceResolver;
    }

    public boolean isGoogleKeyConfigured() {
        return shippingDistanceResolver.isGoogleKeyConfigured();
    }

    public ShippingResult calculateShippingFromNearestBranch(String destinationAddress) {
        List<CoSo_module> branches = cosoRepository.findAll();
        if (branches == null || branches.isEmpty()) {
            throw new RuntimeException("Khong tim thay co so quan de tinh phi ship");
        }
        if (!isGoogleKeyConfigured()) {
            logger.info("Google key missing, using free fallback distance calculation. destination={}",
                    destinationAddress);
        }

        Double bestDistanceKm = null;
        String nearestBranchName = "";
        String nearestBranchId = "";

        for (CoSo_module branch : branches) {
            if (branch == null || branch.getDiaChi() == null || branch.getDiaChi().isBlank()) {
                continue;
            }
            Double distanceKm = shippingDistanceResolver.resolveDistanceKm(branch.getDiaChi(), destinationAddress);
            if (distanceKm == null) {
                logger.warn("Distance cannot be calculated. branchId={}, branchName={}, destination={}",
                        branch.getCosoId(),
                        branch.getTenCs(),
                        destinationAddress);
                continue;
            }

            if (bestDistanceKm == null || distanceKm < bestDistanceKm) {
                bestDistanceKm = distanceKm;
                nearestBranchName = branch.getTenCs() == null ? "" : branch.getTenCs();
                nearestBranchId = branch.getCosoId() == null ? "" : branch.getCosoId();
            }
        }

        if (bestDistanceKm == null) {
            throw new RuntimeException("Chua tinh duoc quang duong tu chi nhanh den dia chi khach hang");
        }

        double finalDistanceKm = bestDistanceKm;
        long shippingFee = (long) Math.ceil(Math.max(0.0, finalDistanceKm)) * 7000L;
        return new ShippingResult(finalDistanceKm, shippingFee, nearestBranchName, nearestBranchId);
    }

    public NhanVien_module resolveServingNhanVienForBranch(String cosoId) {
        if (cosoId == null || cosoId.isBlank()) {
            throw new RuntimeException("Khong xac dinh duoc co so gan nhat de gan nhan vien phu trach");
        }

        List<NhanVien_module> staff = nhanvienRepository.findByCoSo_CosoIdOrderByNhanvienIdAsc(cosoId);
        for (NhanVien_module nv : staff) {
            if (nv == null || nv.getNhanvienId() == null || nv.getNhanvienId().isBlank()) {
                continue;
            }
            if (isServingNhanVien(nv.getChucVu())) {
                return nv;
            }
        }

        throw new RuntimeException("Khong tim thay nhan vien phuc vu tai co so gan nhat");
    }

    public Map<String, Object> buildShippingDiagnostic(String destinationAddress) {
        List<CoSo_module> branches = cosoRepository.findAll();
        List<Map<String, Object>> branchResults = new java.util.ArrayList<>();
        Double bestDistanceKm = null;
        String bestBranchName = "";

        for (CoSo_module branch : branches) {
            if (branch == null || branch.getDiaChi() == null || branch.getDiaChi().isBlank()) {
                continue;
            }
            Map<String, Object> result = shippingDistanceResolver.buildDistanceDiagnostic(branch.getDiaChi(),
                    destinationAddress);
            result.put("branchId", branch.getCosoId());
            result.put("branchName", branch.getTenCs() == null ? "" : branch.getTenCs());
            result.put("branchAddress", branch.getDiaChi());
            branchResults.add(result);

            Object okObj = result.get("ok");
            Object distanceObj = result.get("distanceKm");
            boolean ok = okObj instanceof Boolean && (Boolean) okObj;
            if (ok && distanceObj instanceof Number distance) {
                double km = distance.doubleValue();
                if (bestDistanceKm == null || km < bestDistanceKm) {
                    bestDistanceKm = km;
                    bestBranchName = branch.getTenCs() == null ? "" : branch.getTenCs();
                }
            }
        }

        Long estimatedShipping = bestDistanceKm == null
                ? null
                : (long) Math.ceil(Math.max(0.0, bestDistanceKm)) * 7000L;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("keyConfigured", isGoogleKeyConfigured());
        response.put("destinationAddress", destinationAddress);
        response.put("nearestBranch", bestBranchName);
        response.put("nearestDistanceKm", bestDistanceKm);
        response.put("estimatedShippingFee", estimatedShipping);
        response.put("branchResults", branchResults);
        return response;
    }

    private boolean isServingNhanVien(String chucVu) {
        if (chucVu == null || chucVu.isBlank()) {
            return true;
        }
        String normalized = Normalizer.normalize(chucVu, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        if (normalized.contains("giao hang") || normalized.contains("van chuyen")) {
            return false;
        }
        if (normalized.contains("kho")) {
            return false;
        }
        if (normalized.contains("admin") || normalized.contains("quan ly") || normalized.contains("quan tri")
                || normalized.contains("chu quan")) {
            return false;
        }
        return normalized.contains("nhan vien") || normalized.contains("phuc vu") || normalized.contains("nv")
                || normalized.isBlank();
    }

    public static class ShippingResult {
        private final double distanceKm;
        private final long shippingFee;
        private final String nearestBranchName;
        private final String nearestBranchId;

        public ShippingResult(double distanceKm, long shippingFee, String nearestBranchName, String nearestBranchId) {
            this.distanceKm = distanceKm;
            this.shippingFee = shippingFee;
            this.nearestBranchName = nearestBranchName;
            this.nearestBranchId = nearestBranchId;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public long getShippingFee() {
            return shippingFee;
        }

        public String getNearestBranchName() {
            return nearestBranchName;
        }

        public String getNearestBranchId() {
            return nearestBranchId;
        }
    }
}
