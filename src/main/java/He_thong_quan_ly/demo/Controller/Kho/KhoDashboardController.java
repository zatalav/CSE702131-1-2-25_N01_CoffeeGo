package He_thong_quan_ly.demo.Controller.Kho;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Service.Kho.KhoDashboardService;

@Controller
@RequestMapping("/kho")
public class KhoDashboardController {

    private static final Logger log = LoggerFactory.getLogger(KhoDashboardController.class);
    private static final He_thong_quan_ly.demo.Util.PerfStatsWindow DASHBOARD_LATENCY = new He_thong_quan_ly.demo.Util.PerfStatsWindow(
            100);

    private final KhoDashboardService dashboardService;
    private final QuanlynhanvienRepository nhanvienRepository;

    public KhoDashboardController(
            KhoDashboardService dashboardService,
            QuanlynhanvienRepository nhanvienRepository) {
        this.dashboardService = dashboardService;
        this.nhanvienRepository = nhanvienRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        long startedAt = System.nanoTime();
        model.addAllAttributes(dashboardService.buildDashboardData());
        addUserInfo(model, authentication);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        String username = authentication == null ? null : authentication.getName();
        He_thong_quan_ly.demo.Util.PerfStatsWindow.Snapshot s = DASHBOARD_LATENCY.addAndSnapshot(elapsedMs);
        log.info("[PERF] GET /kho/dashboard user={} elapsed={}ms p95={}ms p99={}ms n={}",
                username,
                elapsedMs,
                s.p95(),
                s.p99(),
                s.count());
        return "kho/Dashboard";
    }

    private void addUserInfo(Model model, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        if (username == null) {
            return;
        }
        var nv = nhanvienRepository.findByGmail(username)
                .or(() -> nhanvienRepository.findById(username))
                .orElse(null);
        if (nv != null) {
            model.addAttribute("currentUserName", nv.getTenNv());
            model.addAttribute("currentUserRole", nv.getChucVu());
        }
    }
}
