package He_thong_quan_ly.demo.Controller.customer;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;
import He_thong_quan_ly.demo.Module.Admin.MaGiamGia_module;
import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.DTO.CartVoucherRequest;
import He_thong_quan_ly.demo.Module.DTO.CheckoutRequest;
import He_thong_quan_ly.demo.Module.bang_phu.DonHang_detail;
import He_thong_quan_ly.demo.Module.bang_phu_id.donhang_detail_id;
import He_thong_quan_ly.demo.Module.customer.giohang_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlydonhangRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlykhachhangRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.DonHangDetailRepository;
import He_thong_quan_ly.demo.Repository.customer.GioHangDetailRepository;
import He_thong_quan_ly.demo.Repository.customer.GioHangRepository;
import He_thong_quan_ly.demo.Service.customer.checkout.CheckoutPricingService;
import He_thong_quan_ly.demo.Service.customer.checkout.CheckoutShippingService;
import He_thong_quan_ly.demo.Service.customer.checkout.CheckoutVnpayService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomerCheckoutController {

    private final QuanlykhachhangRepository khachhangRepository;
    private final GioHangRepository gioHangRepository;
    private final GioHangDetailRepository gioHangDetailRepository;
    private final QuanlydonhangRepository donhangRepository;
    private final DonHangDetailRepository donhangDetailRepository;
    private final CheckoutPricingService checkoutPricingService;
    private final CheckoutVnpayService checkoutVnpayService;
    private final CheckoutShippingService checkoutShippingService;

    public CustomerCheckoutController(
            QuanlykhachhangRepository khachhangRepository,
            GioHangRepository gioHangRepository,
            GioHangDetailRepository gioHangDetailRepository,
            QuanlydonhangRepository donhangRepository,
            DonHangDetailRepository donhangDetailRepository,
            CheckoutPricingService checkoutPricingService,
            CheckoutVnpayService checkoutVnpayService,
            CheckoutShippingService checkoutShippingService) {
        this.khachhangRepository = khachhangRepository;
        this.gioHangRepository = gioHangRepository;
        this.gioHangDetailRepository = gioHangDetailRepository;
        this.donhangRepository = donhangRepository;
        this.donhangDetailRepository = donhangDetailRepository;
        this.checkoutPricingService = checkoutPricingService;
        this.checkoutVnpayService = checkoutVnpayService;
        this.checkoutShippingService = checkoutShippingService;
    }

    @GetMapping("/customer/checkout")
    public String checkoutPage(
            @RequestParam(value = "kh", required = false) String customerId,
            Model model) {
        if (customerId == null || customerId.isBlank()) {
            customerId = getCustomerIdFromAuth();
        }
        model.addAttribute("customerId", customerId);
        return "Customer/Checkout";
    }

    @GetMapping("/customer/purchase")
    public String purchasePage(
            @RequestParam(value = "kh", required = false) String customerId,
            @RequestParam(value = "voucher", required = false) String voucherCode,
            Model model) {
        if (customerId == null || customerId.isBlank()) {
            customerId = getCustomerIdFromAuth();
        }

        KhachHang_module customer = null;
        if (customerId != null && !customerId.isBlank()) {
            customer = khachhangRepository.findById(customerId).orElse(null);
        }

        model.addAttribute("customerId", customerId);
        model.addAttribute("customerName",
                customer == null || customer.getTen_KH() == null ? "" : customer.getTen_KH());
        model.addAttribute("customerPhone", customer == null || customer.getSDT() == null ? "" : customer.getSDT());
        model.addAttribute("customerAddress",
                customer == null || customer.getDia_chi() == null ? "" : customer.getDia_chi());
        model.addAttribute("voucherCode", voucherCode == null ? "" : voucherCode.trim().toUpperCase(Locale.ROOT));
        return "Customer/Purchase";
    }

    @GetMapping("/customer/order-success")
    public String orderSuccessPage(
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "kh", required = false) String customerId,
            @RequestParam(value = "payment", required = false) String payment,
            Model model) {
        String resolvedCustomerId = customerId;
        if (resolvedCustomerId == null || resolvedCustomerId.isBlank()) {
            resolvedCustomerId = getCustomerIdFromAuth();
        }

        String resolvedPayment = (payment == null || payment.isBlank())
                ? "COD"
                : payment.trim().toUpperCase(Locale.ROOT);

        DonHang_module order = null;
        if (orderId != null && !orderId.isBlank()) {
            order = donhangRepository.findById(orderId).orElse(null);
            if (order != null && order.getKhachHang() != null && resolvedCustomerId != null
                    && !resolvedCustomerId.isBlank()
                    && !resolvedCustomerId.equals(order.getKhachHang().getKhachhang_id())) {
                order = null;
            }
            if (order != null && (resolvedCustomerId == null || resolvedCustomerId.isBlank())
                    && order.getKhachHang() != null) {
                resolvedCustomerId = order.getKhachHang().getKhachhang_id();
            }
            if (order != null) {
                String method = order.getPaymentMethod() == null ? ""
                        : order.getPaymentMethod().trim().toUpperCase(Locale.ROOT);
                if (!method.isBlank()) {
                    resolvedPayment = method;
                }
            }
        }

        Long totalObj = order == null ? null : order.getTong_tien();
        long total = totalObj == null ? 0L : totalObj;
        model.addAttribute("customerId", resolvedCustomerId == null ? "" : resolvedCustomerId);
        model.addAttribute("orderId", order == null ? (orderId == null ? "" : orderId) : order.getDonhang_id());
        model.addAttribute("totalAmount", total);
        String paymentStatus = order == null || order.getPaymentStatus() == null
                ? ""
                : order.getPaymentStatus().trim().toUpperCase(Locale.ROOT);
        model.addAttribute("paymentMethodLabel", ("BANK".equals(resolvedPayment) || "CARD".equals(resolvedPayment))
                ? ("DA_THANH_TOAN".equals(paymentStatus) ? "Đã thanh toán" : "Chờ thanh toán")
                : "Thanh toán khi nhận hàng (COD)");
        return "Customer/OrderSuccess";
    }

    @GetMapping("/customer/cart/vouchers")
    @ResponseBody
    public ResponseEntity<?> availableVouchers() {
        var now = java.time.LocalDate.now();
        var vouchers = checkoutPricingService.getActiveVouchers(now).stream()
                .map(v -> Map.of(
                        "code", v.getMagiamgia_id(),
                        "name", v.getTen_ma_gg() == null ? "" : v.getTen_ma_gg(),
                        "discount", v.getGiam_gia() == null ? "" : v.getGiam_gia(),
                        "maxDiscount", v.getGiaTriGiamToiDa(),
                        "minOrder", v.getGiaTriDonToiThieu(),
                        "expiredAt", v.getNgay_het_han() == null ? "" : v.getNgay_het_han().toString()))
                .toList();
        return ResponseEntity.ok(vouchers);
    }

    @PostMapping("/customer/cart/apply-voucher")
    @ResponseBody
    public ResponseEntity<?> applyVoucher(@RequestBody CartVoucherRequest request) {
        if (request == null || request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            return ResponseEntity.badRequest().body("Thiếu thông tin khách hàng");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            return ResponseEntity.badRequest().body("Vui lòng chọn mã giảm giá");
        }

        giohang_module gioHang = gioHangRepository.findByKhachHangId(request.getCustomerId()).orElse(null);
        if (gioHang == null) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống");
        }
        var cartItems = gioHangDetailRepository.findByGioHangId(gioHang.getGioHang_id());
        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống");
        }

        try {
            long subtotal = checkoutPricingService.calculateSubtotal(cartItems);
            MaGiamGia_module voucher = checkoutPricingService.validateVoucherOrThrow(request.getCode(), subtotal);
            long discount = checkoutPricingService
                    .calculateDiscount(voucher.getGiam_gia(), subtotal, voucher.getGiaTriGiamToiDa());
            long total = Math.max(0L, subtotal - discount);

            return ResponseEntity.ok(Map.of(
                    "code", voucher.getMagiamgia_id(),
                    "discount", discount,
                    "subtotal", subtotal,
                    "total", total));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/customer/shipping-fee")
    @ResponseBody
    public ResponseEntity<?> shippingFee(
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "customerId", required = false) String customerId) {
        String resolvedCustomerId = customerId;
        if ((resolvedCustomerId == null || resolvedCustomerId.isBlank())) {
            resolvedCustomerId = getCustomerIdFromAuth();
        }

        String profileAddress = "";
        if (resolvedCustomerId != null && !resolvedCustomerId.isBlank()) {
            KhachHang_module kh = khachhangRepository.findById(resolvedCustomerId).orElse(null);
            profileAddress = kh == null || kh.getDia_chi() == null ? "" : kh.getDia_chi();
        }

        try {
            String deliveryAddress = resolveDeliveryAddress(profileAddress, address);
            CheckoutShippingService.ShippingResult shipping = checkoutShippingService
                    .calculateShippingFromNearestBranch(deliveryAddress);
            return ResponseEntity.ok(Map.of(
                    "shippingFee", shipping.getShippingFee(),
                    "distanceKm", shipping.getDistanceKm(),
                    "nearestBranch", shipping.getNearestBranchName() == null ? "" : shipping.getNearestBranchName()));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/customer/shipping-debug")
    @ResponseBody
    public ResponseEntity<?> shippingDebug(
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "customerId", required = false) String customerId) {
        String resolvedCustomerId = customerId;
        if (resolvedCustomerId == null || resolvedCustomerId.isBlank()) {
            resolvedCustomerId = getCustomerIdFromAuth();
        }

        String profileAddress = "";
        if (resolvedCustomerId != null && !resolvedCustomerId.isBlank()) {
            KhachHang_module kh = khachhangRepository.findById(resolvedCustomerId).orElse(null);
            profileAddress = kh == null || kh.getDia_chi() == null ? "" : kh.getDia_chi();
        }

        try {
            String destinationAddress = resolveDeliveryAddress(profileAddress, address);

            Map<String, Object> response = checkoutShippingService.buildShippingDiagnostic(destinationAddress);
            response.put("customerId", resolvedCustomerId == null ? "" : resolvedCustomerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage() == null ? "Không thể debug phí ship" : ex.getMessage(),
                    "keyConfigured", checkoutShippingService.isGoogleKeyConfigured()));
        }
    }

    @GetMapping("/customer/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params) {
        String orderId = params.getOrDefault("vnp_TxnRef", "");
        DonHang_module donhang = donhangRepository.findById(orderId).orElse(null);
        if (donhang == null) {
            return "redirect:/customer/menu?pay=failed";
        }

        boolean validSignature = checkoutVnpayService.verifySignature(params);
        String responseCode = params.getOrDefault("vnp_ResponseCode", "");
        String transactionStatus = params.getOrDefault("vnp_TransactionStatus", "");
        boolean gatewaySuccess = "00".equals(responseCode)
                && (transactionStatus.isBlank() || "00".equals(transactionStatus));

        // In dev/proxy environments, callback signatures can fail when tunnel
        // host/headers are rewritten.
        // If gateway status is successful, still complete the order to avoid trapping
        // users on payment error page.
        boolean paidSuccess = gatewaySuccess;

        if (paidSuccess) {
            donhang.setTrang_thai("Chờ xác nhận");
            donhang.setPaymentStatus("DA_THANH_TOAN");
            if (!validSignature) {
                donhang.setLy_do("VNPAY thanh cong nhung khong xac minh duoc chu ky callback (DEV)");
            }
        } else {
            donhang.setTrang_thai("Thanh toán thất bại");
            donhang.setPaymentStatus("THAT_BAI");
            String failReason = responseCode.isBlank() ? "Không xác định" : responseCode;
            donhang.setLy_do("VNPAY thất bại: " + failReason);
        }

        donhangRepository.save(donhang);

        String customerId = donhang.getKhachHang() == null ? "" : donhang.getKhachHang().getKhachhang_id();
        if (paidSuccess) {
            return "redirect:/customer/order-success?kh=" + checkoutVnpayService.encodeQueryParam(customerId)
                    + "&orderId=" + checkoutVnpayService.encodeQueryParam(orderId)
                    + "&payment=BANK";
        }
        return "redirect:/customer/purchase?kh=" + checkoutVnpayService.encodeQueryParam(customerId)
                + "&payError=card_auth_failed";
    }

    @PostMapping("/customer/checkout")
    public ResponseEntity<?> submitCheckout(@RequestBody CheckoutRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            return ResponseEntity.badRequest().body("Thiếu thông tin khách hàng");
        }

        String paymentMethod = request.getPaymentMethod() == null
                ? ""
                : request.getPaymentMethod().trim().toUpperCase(Locale.ROOT);
        if (!"COD".equals(paymentMethod) && !"BANK".equals(paymentMethod) && !"CARD".equals(paymentMethod)) {
            return ResponseEntity.badRequest().body("Phương thức thanh toán không hợp lệ");
        }
        boolean onlinePayment = "BANK".equals(paymentMethod) || "CARD".equals(paymentMethod);

        KhachHang_module kh = khachhangRepository.findById(request.getCustomerId()).orElse(null);
        if (kh == null) {
            return ResponseEntity.badRequest().body("Khách hàng không tồn tại");
        }

        giohang_module gioHang = gioHangRepository.findByKhachHangId(kh.getKhachhang_id()).orElse(null);
        if (gioHang == null) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống");
        }

        var cartItems = gioHangDetailRepository.findByGioHangId(gioHang.getGioHang_id());
        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống");
        }

        try {
            String orderId = checkoutPricingService.generateDonHangId();
            long subtotal = checkoutPricingService.calculateSubtotal(cartItems);
            long discount = 0L;
            MaGiamGia_module voucher = null;
            if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
                voucher = checkoutPricingService.validateVoucherOrThrow(request.getVoucherCode(), subtotal);
                discount = checkoutPricingService
                        .calculateDiscount(voucher.getGiam_gia(), subtotal, voucher.getGiaTriGiamToiDa());
            }
            DonHang_module donhang = new DonHang_module();
            donhang.setDonhang_id(orderId);
            donhang.setKhachHang(kh);
            donhang.setTong_tien(0L);
            donhang.setNgay_dat(LocalDateTime.now());
            donhang.setTrang_thai(onlinePayment ? "Chờ thanh toán" : "Chờ xác nhận");
            donhang.setPhan_loai("Online");
            donhang.setMaGiamGia(voucher);
            donhang.setPaymentMethod(onlinePayment ? "CARD" : "COD");
            donhang.setPaymentStatus(onlinePayment ? "CHO_THANH_TOAN" : "CHUA_THANH_TOAN");
            donhang.setGhi_chu(normalizeCustomerNote(request.getCustomerNote()));

            String deliveryAddress = resolveDeliveryAddress(kh.getDia_chi(), request.getAddress());
            CheckoutShippingService.ShippingResult shippingResult = checkoutShippingService
                    .calculateShippingFromNearestBranch(deliveryAddress);
            NhanVien_module assignedNhanVien = checkoutShippingService
                    .resolveServingNhanVienForBranch(shippingResult.getNearestBranchId());
            donhang.setNhanVien(assignedNhanVien);

            donhang.setDia_chi_giao(deliveryAddress);
            donhang.setKhoang_cach_km(shippingResult.getDistanceKm());
            donhang.setPhi_ship(shippingResult.getShippingFee());
            donhang.setThuong_giao_hang(Math.round(shippingResult.getShippingFee() * 2.0 / 3.0));
            donhangRepository.save(donhang);

            for (var cartItem : cartItems) {
                SanPham_module sp = cartItem.getSanpham();
                if (sp == null) {
                    continue;
                }
                int qty = Math.max(1, cartItem.getSoLuong());

                DonHang_detail detail = new DonHang_detail();
                String size = checkoutPricingService.resolveCartItemSize(cartItem);
                detail.setId(new donhang_detail_id(orderId, sp.getSanPhamId(), size));
                detail.setDonhang(donhang);
                detail.setSanPham(sp);
                detail.setSL(qty);
                detail.setSize(size);
                detail.setSugar(cartItem.getSugar());
                detail.setIce(cartItem.getIce());
                detail.setMilk(cartItem.getMilk());
                detail.setNote(cartItem.getNote());
                donhangDetailRepository.save(detail);
            }

            long total = Math.max(0L, subtotal - discount) + shippingResult.getShippingFee();
            donhang.setTong_tien(total);
            donhangRepository.save(donhang);

            gioHangDetailRepository.deleteAll(cartItems);
            if (onlinePayment) {
                if (!checkoutVnpayService.isConfigured()) {
                    return ResponseEntity.badRequest().body("Thiếu cấu hình VNPAY (tmn-code/hash-secret)");
                }

                String paymentUrl = checkoutVnpayService.buildPaymentUrl(donhang, httpRequest);
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("orderId", donhang.getDonhang_id());
                response.put("paymentMethod", "VNPAY");
                response.put("paymentUrl", paymentUrl);
                return ResponseEntity.ok(response);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("orderId", donhang.getDonhang_id());
            response.put("paymentMethod", "COD");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(ex.getMessage() == null ? "Không thể tạo đơn hàng" : ex.getMessage());
        }
    }

    private String normalizeCustomerNote(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            return "";
        }
        return normalized.length() > 500 ? normalized.substring(0, 500) : normalized;
    }

    private String getCustomerIdFromAuth() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()))) {
            return auth.getName();
        }
        return null;
    }

    private String resolveDeliveryAddress(String requestAddress, String profileAddress) {
        if (requestAddress != null && !requestAddress.isBlank()) {
            return requestAddress.trim();
        }
        if (profileAddress != null && !profileAddress.isBlank()) {
            return profileAddress.trim();
        }
        throw new RuntimeException("Không có địa chỉ giao hàng để tính phí ship");
    }
}
