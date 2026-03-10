package He_thong_quan_ly.demo.Config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Service.Admin.QuanlydonhangService;
import He_thong_quan_ly.demo.Service.Kho.KhoMasterDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupConfig {

    private final KhoMasterDataService khoMasterDataService;
    private final QuanlydonhangService quanlydonhangService;
    private final QuanlynhanvienRepository quanlynhanvienRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupKhoMasterDataCache() {
        try {
            khoMasterDataService.getMasterData();
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
    }
}
