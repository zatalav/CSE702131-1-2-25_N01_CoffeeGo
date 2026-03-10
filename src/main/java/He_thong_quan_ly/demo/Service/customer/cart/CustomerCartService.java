package He_thong_quan_ly.demo.Service.customer.cart;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;
import He_thong_quan_ly.demo.Module.Admin.SanPhamVariant_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.DTO.CartAddRequest;
import He_thong_quan_ly.demo.Module.DTO.CartQuantityRequest;
import He_thong_quan_ly.demo.Module.DTO.CartRemoveRequest;
import He_thong_quan_ly.demo.Module.bang_phu.giohang_detail;
import He_thong_quan_ly.demo.Module.bang_phu_id.giohang_detail_id;
import He_thong_quan_ly.demo.Module.customer.giohang_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlykhachhangRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlysanphamRepository;
import He_thong_quan_ly.demo.Repository.Admin.SanPhamVariantRepository;
import He_thong_quan_ly.demo.Repository.customer.GioHangDetailRepository;
import He_thong_quan_ly.demo.Repository.customer.GioHangRepository;

@Service
public class CustomerCartService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerCartService.class);

    private final GioHangRepository gioHangRepository;
    private final GioHangDetailRepository gioHangDetailRepository;
    private final QuanlykhachhangRepository khachhangRepository;
    private final QuanlysanphamRepository sanphamRepository;
    private final SanPhamVariantRepository variantRepository;

    public CustomerCartService(
            GioHangRepository gioHangRepository,
            GioHangDetailRepository gioHangDetailRepository,
            QuanlykhachhangRepository khachhangRepository,
            QuanlysanphamRepository sanphamRepository,
            SanPhamVariantRepository variantRepository) {
        this.gioHangRepository = gioHangRepository;
        this.gioHangDetailRepository = gioHangDetailRepository;
        this.khachhangRepository = khachhangRepository;
        this.sanphamRepository = sanphamRepository;
        this.variantRepository = variantRepository;
    }

    public ResponseEntity<?> addToCart(CartAddRequest request, String currentCustomerId) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin yêu cầu");
            }
            if (isBlank(request.getCustomerId())) {
                request.setCustomerId(currentCustomerId);
            }
            if (isBlank(request.getCustomerId())) {
                return ResponseEntity.badRequest().body("Thiếu thông tin khách hàng");
            }
            if (isBlank(request.getProductId())) {
                return ResponseEntity.badRequest().body("Thiếu sản phẩm");
            }

            KhachHang_module kh = khachhangRepository.findById(request.getCustomerId()).orElse(null);
            if (kh == null) {
                return ResponseEntity.badRequest().body("Khách hàng không tồn tại");
            }

            SanPham_module sp = sanphamRepository.findById(request.getProductId()).orElse(null);
            if (sp == null) {
                return ResponseEntity.badRequest().body("Sản phẩm không tồn tại");
            }

            giohang_module gioHang = resolveOrCreateCart(kh);
            String normalizedSize = resolveSize(request.getSize(), sp.getSanPhamId());

            giohang_detail detail = gioHangDetailRepository
                    .findByGioHangIdAndSanPhamIdAndSize(gioHang.getGioHang_id(), sp.getSanPhamId(), normalizedSize)
                    .orElse(null);

            if (detail == null) {
                detail = new giohang_detail();
                detail.setId(new giohang_detail_id(gioHang.getGioHang_id(), sp.getSanPhamId(), normalizedSize));
                detail.setGiohang(gioHang);
                detail.setSanpham(sp);
                detail.setSoLuong(0);
            }

            int qty = Math.max(1, request.getQty());
            detail.setSoLuong(detail.getSoLuong() + qty);
            detail.setSize(normalizedSize);
            detail.setSugar(defaultText(request.getSugar()));
            detail.setIce(defaultText(request.getIce()));
            detail.setMilk(defaultText(request.getMilk()));
            detail.setNote(defaultText(request.getNote()));

            ensureDetailRelation(detail, gioHang, sp.getSanPhamId(), normalizedSize);
            saveWithDuplicateFallback(detail, gioHang.getGioHang_id(), sp.getSanPhamId(), qty, request, normalizedSize);

            long unitPrice = resolveUnitPrice(sp.getSanPhamId(), normalizedSize, sp.getGia());
            return ResponseEntity.ok(Map.of(
                    "message", "Thêm sản phẩm thành công",
                    "size", normalizedSize,
                    "unitPrice", unitPrice));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Lỗi thêm giỏ hàng: " + ex.getMessage());
        }
    }

    public ResponseEntity<?> removeFromCart(CartRemoveRequest request, String currentCustomerId) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin yêu cầu");
            }

            String customerId = isBlank(request.getCustomerId()) ? currentCustomerId : request.getCustomerId();
            if (isBlank(customerId)) {
                return ResponseEntity.badRequest().body("Thiếu thông tin khách hàng");
            }
            if (isBlank(request.getProductId())) {
                return ResponseEntity.badRequest().body("Thiếu sản phẩm");
            }

            giohang_module gioHang = gioHangRepository.findByKhachHangId(customerId).orElse(null);
            if (gioHang == null) {
                return ResponseEntity.badRequest().body("Giỏ hàng không tồn tại");
            }

            String normalizedSize = normalizeSize(request.getSize());
            giohang_detail detail = gioHangDetailRepository
                    .findByGioHangIdAndSanPhamIdAndSize(gioHang.getGioHang_id(), request.getProductId(), normalizedSize)
                    .orElse(null);

            if (detail == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm cần xóa trong giỏ");
            }

            gioHangDetailRepository.delete(detail);
            long count = gioHangDetailRepository.sumQuantityByGioHangId(gioHang.getGioHang_id());
            return ResponseEntity.ok(Map.of(
                    "message", "Xóa sản phẩm khỏi giỏ thành công",
                    "count", count));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Lỗi xóa sản phẩm khỏi giỏ: " + ex.getMessage());
        }
    }

    public ResponseEntity<?> updateQuantity(CartQuantityRequest request, String currentCustomerId) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin yêu cầu");
            }

            String customerId = isBlank(request.getCustomerId()) ? currentCustomerId : request.getCustomerId();
            if (isBlank(customerId)) {
                return ResponseEntity.badRequest().body("Thiếu thông tin khách hàng");
            }
            if (isBlank(request.getProductId())) {
                return ResponseEntity.badRequest().body("Thiếu sản phẩm");
            }

            giohang_module gioHang = gioHangRepository.findByKhachHangId(customerId).orElse(null);
            if (gioHang == null) {
                return ResponseEntity.badRequest().body("Giỏ hàng không tồn tại");
            }

            String normalizedSize = normalizeSize(request.getSize());
            giohang_detail detail = gioHangDetailRepository
                    .findByGioHangIdAndSanPhamIdAndSize(gioHang.getGioHang_id(), request.getProductId(), normalizedSize)
                    .orElse(null);

            if (detail == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm cần cập nhật trong giỏ");
            }

            int qty = request.getQty();
            if (qty <= 0) {
                gioHangDetailRepository.delete(detail);
            } else {
                detail.setSoLuong(qty);
                gioHangDetailRepository.save(detail);
            }

            long count = gioHangDetailRepository.sumQuantityByGioHangId(gioHang.getGioHang_id());
            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật số lượng thành công",
                    "count", count));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật số lượng: " + ex.getMessage());
        }
    }

    public ResponseEntity<?> count(String customerId) {
        if (isBlank(customerId)) {
            return ResponseEntity.ok(Map.of("count", 0));
        }

        giohang_module gioHang = gioHangRepository.findByKhachHangId(customerId).orElse(null);
        if (gioHang == null) {
            return ResponseEntity.ok(Map.of("count", 0));
        }

        long count = gioHangDetailRepository.sumQuantityByGioHangId(gioHang.getGioHang_id());
        return ResponseEntity.ok(Map.of("count", count));
    }

    public ResponseEntity<?> items(String customerId) {
        if (isBlank(customerId)) {
            return ResponseEntity.ok(List.of());
        }

        giohang_module gioHang = gioHangRepository.findByKhachHangId(customerId).orElse(null);
        if (gioHang == null) {
            return ResponseEntity.ok(List.of());
        }

        var items = gioHangDetailRepository.findByGioHangId(gioHang.getGioHang_id()).stream()
                .map(this::toCartItem)
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(items);
    }

    private Map<String, Object> toCartItem(giohang_detail detail) {
        var sp = detail.getSanpham();
        if (sp == null) {
            return null;
        }

        String rawSize = detail.getSize();
        if ((rawSize == null || rawSize.isBlank()) && detail.getId() != null) {
            rawSize = detail.getId().getSize();
        }

        String size = normalizeSize(rawSize);
        long unitPrice = resolveUnitPrice(sp.getSanPhamId(), size, sp.getGia());
        int qty = Math.max(1, detail.getSoLuong());

        Map<String, Object> item = new HashMap<>();
        item.put("productId", sp.getSanPhamId());
        item.put("name", sp.getTenSp());
        item.put("price", unitPrice);
        item.put("unitPrice", unitPrice);
        item.put("qty", qty);
        item.put("quantity", qty);
        item.put("size", size);
        item.put("lineTotal", unitPrice * qty);
        item.put("sugar", defaultText(detail.getSugar()));
        item.put("ice", defaultText(detail.getIce()));
        item.put("milk", defaultText(detail.getMilk()));
        item.put("note", defaultText(detail.getNote()));
        return item;
    }

    private giohang_module resolveOrCreateCart(KhachHang_module kh) {
        giohang_module gioHang = gioHangRepository.findByKhachHangId(kh.getKhachhang_id()).orElse(null);
        if (gioHang != null) {
            return gioHang;
        }

        giohang_module newCart = new giohang_module();
        newCart.setGioHang_id(generateGioHangId());
        newCart.setKhachHang(kh);
        return gioHangRepository.save(newCart);
    }

    private String resolveSize(String size, String productId) {
        String normalized = normalizeSize(size);
        if (!isBlank(normalized)) {
            return normalized;
        }
        return variantRepository.findFirstBySanPham_SanPhamIdOrderByPriceAsc(productId)
                .map(SanPhamVariant_module::getSize)
                .map(this::normalizeSize)
                .orElse("M");
    }

    private void ensureDetailRelation(giohang_detail detail, giohang_module gioHang, String productId, String size) {
        try {
            if (detail.getId() == null) {
                detail.setId(new giohang_detail_id(gioHang.getGioHang_id(), productId, size));
            } else {
                detail.getId().setGioHang_id(gioHang.getGioHang_id());
                detail.getId().setSanpham_id(productId);
                detail.getId().setSize(size);
            }
            if (detail.getGiohang() == null) {
                detail.setGiohang(gioHang);
            }
        } catch (RuntimeException logEx) {
            logger.warn("Failed to log cart state before save", logEx);
        }
    }

    private void saveWithDuplicateFallback(
            giohang_detail detail,
            String gioHangId,
            String productId,
            int qty,
            CartAddRequest request,
            String normalizedSize) {
        try {
            gioHangDetailRepository.save(detail);
        } catch (DataIntegrityViolationException duplicateKeyEx) {
            var existingByProduct = gioHangDetailRepository.findAllByGioHangIdAndSanPhamId(gioHangId, productId);
            if (existingByProduct.isEmpty()) {
                throw duplicateKeyEx;
            }

            giohang_detail legacyDetail = existingByProduct.get(0);
            legacyDetail.setSoLuong(Math.max(1, legacyDetail.getSoLuong()) + qty);
            legacyDetail.setSugar(defaultText(request.getSugar()));
            legacyDetail.setIce(defaultText(request.getIce()));
            legacyDetail.setMilk(defaultText(request.getMilk()));
            legacyDetail.setNote(defaultText(request.getNote()));
            if (isBlank(legacyDetail.getSize())) {
                legacyDetail.setSize(normalizedSize);
            }
            gioHangDetailRepository.save(legacyDetail);
        }
    }

    private String generateGioHangId() {
        String maxId = gioHangRepository.findMaxId();
        if (isBlank(maxId)) {
            return "GH001";
        }

        String digits = maxId.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return "GH001";
        }

        int number = Integer.parseInt(digits);
        return String.format("GH%03d", number + 1);
    }

    private String normalizeSize(String size) {
        if (isBlank(size)) {
            return "M";
        }
        return size.trim().toUpperCase(Locale.ROOT);
    }

    private long resolveUnitPrice(String sanPhamId, String size, Long fallbackGia) {
        if (!isBlank(sanPhamId)) {
            if (!isBlank(size)) {
                var exact = variantRepository.findFirstBySanPham_SanPhamIdAndSizeIgnoreCase(sanPhamId, size);
                if (exact.isPresent()) {
                    return Math.round(exact.get().getPrice());
                }
            }

            var first = variantRepository.findFirstBySanPham_SanPhamIdOrderByPriceAsc(sanPhamId);
            if (first.isPresent()) {
                return Math.round(first.get().getPrice());
            }
        }

        return fallbackGia == null ? 0L : fallbackGia;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
