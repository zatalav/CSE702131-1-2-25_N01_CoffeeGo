package He_thong_quan_ly.demo.Service.customer.checkout;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import He_thong_quan_ly.demo.Module.Admin.MaGiamGia_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.bang_phu.giohang_detail;
import He_thong_quan_ly.demo.Repository.Admin.QuanlydonhangRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlymagiamgiaRepository;
import He_thong_quan_ly.demo.Repository.Admin.SanPhamVariantRepository;

@Service
public class CheckoutPricingService {

    private final QuanlydonhangRepository donhangRepository;
    private final QuanlymagiamgiaRepository magiamgiaRepository;
    private final SanPhamVariantRepository variantRepository;

    public CheckoutPricingService(
            QuanlydonhangRepository donhangRepository,
            QuanlymagiamgiaRepository magiamgiaRepository,
            SanPhamVariantRepository variantRepository) {
        this.donhangRepository = donhangRepository;
        this.magiamgiaRepository = magiamgiaRepository;
        this.variantRepository = variantRepository;
    }

    public long calculateSubtotal(List<giohang_detail> cartItems) {
        PriceLookup priceLookup = buildPriceLookup(cartItems);
        long subtotal = 0L;
        for (var cartItem : cartItems) {
            SanPham_module sp = cartItem.getSanpham();
            if (sp == null) {
                continue;
            }
            int qty = Math.max(1, cartItem.getSoLuong());
            String size = resolveCartItemSize(cartItem);
            long unitPrice = resolveUnitPrice(priceLookup, sp.getSanPhamId(), size, sp.getGia());
            subtotal += unitPrice * qty;
        }
        return subtotal;
    }

    public List<MaGiamGia_module> getActiveVouchers(LocalDate now) {
        LocalDate safeNow = now == null ? LocalDate.now() : now;
        return magiamgiaRepository.findActiveVouchers(safeNow);
    }

    public MaGiamGia_module validateVoucherOrThrow(String code, long subtotal) {
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        MaGiamGia_module voucher = magiamgiaRepository.findById(normalizedCode).orElse(null);
        if (voucher == null) {
            throw new RuntimeException("Ma giam gia khong ton tai");
        }
        if (voucher.getTrang_thai() == null || !"HOAT_DONG".equalsIgnoreCase(voucher.getTrang_thai())) {
            throw new RuntimeException("Ma giam gia hien khong kha dung");
        }
        if (voucher.getNgay_het_han() != null && voucher.getNgay_het_han().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("Ma giam gia da het han");
        }
        if (subtotal < voucher.getGiaTriDonToiThieu()) {
            throw new RuntimeException("Don hang chua dat gia tri toi thieu de ap dung ma");
        }
        return voucher;
    }

    public long calculateDiscount(String giamGia, long subtotal, long maxDiscountAmount) {
        if (giamGia == null || giamGia.isBlank()) {
            return 0L;
        }
        String value = giamGia.trim().replace(" ", "");
        long cap = Math.max(0L, maxDiscountAmount);
        try {
            if (value.endsWith("%")) {
                String percentPart = value.substring(0, value.length() - 1).replace(",", ".");
                double percent = Double.parseDouble(percentPart);
                long discount = Math.round(subtotal * (percent / 100.0));
                if (cap > 0L) {
                    discount = Math.min(discount, cap);
                }
                return Math.max(0L, Math.min(discount, subtotal));
            }

            String digits = value.replaceAll("[^0-9]", "");
            if (digits.isBlank()) {
                return 0L;
            }
            long fixed = Long.parseLong(digits);
            long discount = fixed;
            if (cap > 0L) {
                discount = Math.min(discount, cap);
            }
            return Math.max(0L, Math.min(discount, subtotal));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    public String generateDonHangId() {
        List<String> ids = donhangRepository.findAllIdDesc();
        if (ids.isEmpty()) {
            return "DH001";
        }
        String last = ids.get(0);
        if (last == null || last.length() < 3) {
            return "DH001";
        }
        int number = Integer.parseInt(last.substring(2));
        return String.format("DH%03d", number + 1);
    }

    public String resolveCartItemSize(giohang_detail cartItem) {
        if (cartItem == null) {
            return "M";
        }
        String rawSize = cartItem.getSize();
        if ((rawSize == null || rawSize.isBlank()) && cartItem.getId() != null) {
            rawSize = cartItem.getId().getSize();
        }
        return normalizeSize(rawSize);
    }

    public long resolveUnitPrice(String sanPhamId, String size, Long fallbackGia) {
        if (sanPhamId != null && !sanPhamId.isBlank()) {
            if (size != null && !size.isBlank()) {
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

    private long resolveUnitPrice(PriceLookup priceLookup, String sanPhamId, String size, Long fallbackGia) {
        if (sanPhamId != null && !sanPhamId.isBlank() && priceLookup != null) {
            Long exact = priceLookup.priceByProductAndSize().get(productSizeKey(sanPhamId, normalizeSize(size)));
            if (exact != null) {
                return exact;
            }

            Long minPrice = priceLookup.minPriceByProduct().get(sanPhamId);
            if (minPrice != null) {
                return minPrice;
            }
        }
        return fallbackGia == null ? 0L : fallbackGia;
    }

    private PriceLookup buildPriceLookup(List<giohang_detail> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return new PriceLookup(Map.of(), Map.of());
        }

        Set<String> productIds = new LinkedHashSet<>();
        for (giohang_detail cartItem : cartItems) {
            if (cartItem == null || cartItem.getSanpham() == null) {
                continue;
            }
            String productId = cartItem.getSanpham().getSanPhamId();
            if (productId == null || productId.isBlank()) {
                continue;
            }
            productIds.add(productId);
        }

        if (productIds.isEmpty()) {
            return new PriceLookup(Map.of(), Map.of());
        }

        Map<String, Long> minPriceByProduct = new HashMap<>();
        Map<String, Long> priceByProductAndSize = new HashMap<>();
        for (var variant : variantRepository.findBySanPham_SanPhamIdIn(List.copyOf(productIds))) {
            if (variant == null || variant.getSanPham() == null || variant.getSanPham().getSanPhamId() == null
                    || variant.getSanPham().getSanPhamId().isBlank()) {
                continue;
            }

            String productId = variant.getSanPham().getSanPhamId();
            long price = Math.round(variant.getPrice());
            minPriceByProduct.merge(productId, price, Math::min);
            priceByProductAndSize.put(productSizeKey(productId, normalizeSize(variant.getSize())), price);
        }

        return new PriceLookup(minPriceByProduct, priceByProductAndSize);
    }

    private String productSizeKey(String productId, String size) {
        return productId + "::" + normalizeSize(size);
    }

    private String normalizeSize(String size) {
        if (size == null || size.isBlank()) {
            return "M";
        }
        return size.trim().toUpperCase(Locale.ROOT);
    }

    private record PriceLookup(Map<String, Long> minPriceByProduct, Map<String, Long> priceByProductAndSize) {
    }
}
