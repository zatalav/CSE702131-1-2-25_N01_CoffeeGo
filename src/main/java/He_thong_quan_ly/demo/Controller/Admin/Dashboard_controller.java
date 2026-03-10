package He_thong_quan_ly.demo.Controller.Admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;
import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlydonhangRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlykhachhangRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.DonHangDetailRepository;

@Controller
public class Dashboard_controller {
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Admin/Dashboard";
    }
}

@RestController
@RequestMapping("/api/dashboard")
class DashboardApiController {

    private final QuanlydonhangRepository donhangRepository;
    private final QuanlykhachhangRepository khachhangRepository;
    private final QuanlynguyenlieuRepository nguyenlieuRepository;
    private final DonHangDetailRepository donHangDetailRepository;

    DashboardApiController(
            QuanlydonhangRepository donhangRepository,
            QuanlykhachhangRepository khachhangRepository,
            QuanlynguyenlieuRepository nguyenlieuRepository,
            DonHangDetailRepository donHangDetailRepository) {
        this.donhangRepository = donhangRepository;
        this.khachhangRepository = khachhangRepository;
        this.nguyenlieuRepository = nguyenlieuRepository;
        this.donHangDetailRepository = donHangDetailRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);
        int currentYear = today.getYear();

        List<DonHang_module> allOrders = donhangRepository.findAll();

        long todayRevenue = 0L;
        long yesterdayRevenue = 0L;
        int todayOrders = 0;
        int yesterdayOrders = 0;
        long totalRevenue = 0L;

        LinkedHashMap<Integer, Long> revenueByMonth = new LinkedHashMap<>();
        IntStream.rangeClosed(1, 12).forEach(month -> revenueByMonth.put(month, 0L));

        for (DonHang_module order : allOrders) {
            if (isCancelled(order.getTrang_thai())) {
                continue;
            }

            Long total = Optional.ofNullable(order.getTong_tien()).orElse(0L);
            totalRevenue += total;

            java.time.LocalDate orderDate = order.getNgay_dat() == null ? null : order.getNgay_dat().toLocalDate();
            if (orderDate == null) {
                continue;
            }
            if (orderDate.equals(today)) {
                todayRevenue += total;
                todayOrders++;
            }
            if (orderDate.equals(yesterday)) {
                yesterdayRevenue += total;
                yesterdayOrders++;
            }

            if (orderDate.getYear() == currentYear) {
                int month = orderDate.getMonthValue();
                revenueByMonth.put(month, revenueByMonth.get(month) + total);
            }
        }

        List<KhachHang_module> customers = khachhangRepository.findAll();
        long activeCustomers = customers.stream()
                .filter(customer -> {
                    String status = customer.getTrang_thai();
                    return status == null || !status.equalsIgnoreCase("KHOA");
                })
                .count();

        List<NguyenLieu_module> ingredients = nguyenlieuRepository.findAll();
        long lowStock = ingredients.stream()
                .filter(ingredient -> ingredient.getSlTon() > 0 && ingredient.getSlTon() < 10)
                .count();

        double revenuePercent = percentageChange(todayRevenue, yesterdayRevenue);
        double ordersPercent = percentageChange(todayOrders, yesterdayOrders);

        List<Long> revenueSeries = new ArrayList<>(revenueByMonth.values());
        List<String> monthLabels = revenueByMonth.keySet().stream()
                .map(month -> "T" + month)
                .toList();

        stats.put("todayRevenue", todayRevenue);
        stats.put("revenuePercent", Math.round(revenuePercent * 10.0) / 10.0);
        stats.put("todayOrders", todayOrders);
        stats.put("ordersPercent", Math.round(ordersPercent * 10.0) / 10.0);
        stats.put("totalCustomers", customers.size());
        stats.put("onlineCustomers", activeCustomers);
        stats.put("lowStock", lowStock);
        stats.put("totalRevenue", totalRevenue);
        stats.put("revenueSeries", revenueSeries);
        stats.put("monthLabels", monthLabels);
        return stats;
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> getOrders() {
        List<Map<String, Object>> orders = new ArrayList<>();
        List<DonHang_module> recentOrders = donhangRepository.findRecentOrders(PageRequest.of(0, 8));

        for (DonHang_module order : recentOrders) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", order.getDonhang_id());
            row.put("customer", order.getKhachHang() == null ? "Khách lẻ" : order.getKhachHang().getTen_KH());
            row.put("statusText", Optional.ofNullable(order.getTrang_thai()).orElse("Chưa xác định"));
            row.put("status", toStatusClass(order.getTrang_thai()));
            row.put("total", Optional.ofNullable(order.getTong_tien()).orElse(0L));
            orders.add(row);
        }

        return orders;
    }

    @GetMapping("/top-products")
    public List<Map<String, Object>> getTopProducts() {
        List<Map<String, Object>> products = new ArrayList<>();
        java.time.LocalDate now = java.time.LocalDate.now();
        List<Object[]> rows = donHangDetailRepository.findTopProductSalesByMonth(
                now.getYear(),
                now.getMonthValue(),
                PageRequest.of(0, 5));

        for (Object[] row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", String.valueOf(row[0]));
            item.put("sold", row[1] == null ? 0L : ((Number) row[1]).longValue());
            products.add(item);
        }

        return products;
    }

    @GetMapping("/inventory")
    public List<Map<String, Object>> getInventory() {
        List<Map<String, Object>> inventory = new ArrayList<>();
        List<NguyenLieu_module> ingredients = nguyenlieuRepository.findAll();
        ingredients.sort(Comparator.comparingInt(NguyenLieu_module::getSlTon));

        int limit = Math.min(6, ingredients.size());
        for (int i = 0; i < limit; i++) {
            NguyenLieu_module ingredient = ingredients.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("name", ingredient.getTenNguyenLieu());
            String unit = Optional.ofNullable(ingredient.getDonVi()).orElse("").trim();
            item.put("quantity", ingredient.getSlTon() + (unit.isBlank() ? "" : " " + unit));
            item.put("status", stockStatus(ingredient.getSlTon()));
            inventory.add(item);
        }
        return inventory;

    }

    private double percentageChange(double current, double previous) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    private String stockStatus(int stock) {
        if (stock <= 0) {
            return "Hết hàng";
        }
        if (stock < 10) {
            return "Sắp hết";
        }
        return "Đủ hàng";
    }

    private boolean isCancelled(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.toLowerCase(Locale.ROOT);
        return normalized.contains("hủy") || normalized.contains("huy") || normalized.contains("cancel");
    }

    private String toStatusClass(String status) {
        if (status == null) {
            return "pending";
        }
        String normalized = status.toLowerCase(Locale.ROOT);
        if (normalized.contains("giao") || normalized.contains("hoàn") || normalized.contains("hoan")
                || normalized.contains("xác nhận") || normalized.contains("xac nhan")) {
            return "done";
        }
        return "pending";
    }
}
