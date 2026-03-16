package He_thong_quan_ly.demo.Config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Service.Admin.QuanlydanhmucService;
import He_thong_quan_ly.demo.Service.Admin.QuanlysanphamService;
import He_thong_quan_ly.demo.Service.Admin.QuanlydonhangService;
import He_thong_quan_ly.demo.Service.Kho.KhoDashboardService;
import He_thong_quan_ly.demo.Service.Kho.KhoMasterDataService;
import He_thong_quan_ly.demo.Service.Kho.NhapKhoService;
import He_thong_quan_ly.demo.Service.Kho.XuatKhoService;
import He_thong_quan_ly.demo.Service.customer.checkout.CheckoutPricingService;
import He_thong_quan_ly.demo.Service.customer.checkout.CheckoutShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupConfig {

    private final KhoMasterDataService khoMasterDataService;
    private final KhoDashboardService khoDashboardService;
    private final NhapKhoService nhapKhoService;
    private final XuatKhoService xuatKhoService;
    private final QuanlydonhangService quanlydonhangService;
    private final QuanlynhanvienRepository quanlynhanvienRepository;
    private final QuanlysanphamService quanlysanphamService;
    private final QuanlydanhmucService quanlydanhmucService;
    private final CheckoutPricingService checkoutPricingService;
    private final CheckoutShippingService checkoutShippingService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupKhoMasterDataCache() {
        try {
            khoMasterDataService.getMasterData();
            khoDashboardService.buildDashboardData();
            nhapKhoService.findPaged(0, 20, "");
            xuatKhoService.findPaged(0, 20, "");
            xuatKhoService.findTonKhoPaged(PageRequest.of(0, 20), "");
            log.info("Kho master-data cache warmed up successfully");
        } catch (Exception ex) {
            log.warn("Unable to warm up kho master-data cache at startup: {}", ex.getMessage());
        }

        try {
            // Warm global delivery list cache used by giao-hang page.
            quanlydonhangService.getConfirmedOrdersForDelivery();

            // Warm user-scoped delivery caches for a small set of employees to reduce
            // first-hit latency.
            var nhanVienPage = quanlynhanvienRepository.findAll(PageRequest.of(0, 5));
            nhanVienPage.getContent().forEach(nv -> {
                String principal = (nv.getGmail() == null || nv.getGmail().isBlank())
                        ? nv.getNhanvienId()
                        : nv.getGmail();
                if (principal != null && !principal.isBlank()) {
                    quanlydonhangService.getDeliveryDashboardStats(principal);
                    quanlydonhangService.getOrdersInDelivery(principal);
                    quanlydonhangService.getCancelledDeliveryOrders(principal);
                }
            });

            log.info("Delivery caches warmed up successfully");
        } catch (Exception ex) {
            log.warn("Unable to warm up delivery caches at startup: {}", ex.getMessage());
        }

        try {
            // Warm core customer browsing caches.
            quanlysanphamService.findActiveProductsPaged(24);
            quanlydanhmucService.findAll();
            checkoutPricingService.getActiveVouchers(java.time.LocalDate.now());

            // Warm one shipping estimate entry to reduce first-hit latency.
            checkoutShippingService.calculateShippingFromNearestBranch("Dong Da, Ha Noi, Vietnam");
            log.info("Customer menu/checkout caches warmed up successfully");
        } catch (Exception ex) {
            log.warn("Unable to warm up customer caches at startup: {}", ex.getMessage());
        }
    }
}
