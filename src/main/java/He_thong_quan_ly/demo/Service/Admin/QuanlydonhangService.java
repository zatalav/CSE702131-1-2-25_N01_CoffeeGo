package He_thong_quan_ly.demo.Service.Admin;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.DonHang_module;
import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;
import He_thong_quan_ly.demo.Module.Admin.SanPham_module;
import He_thong_quan_ly.demo.Module.DTO.OrderCreateRequest;
import He_thong_quan_ly.demo.Module.DTO.OrderItemRequest;
import He_thong_quan_ly.demo.Module.NhanVienKho.Khobep_module;
import He_thong_quan_ly.demo.Module.bang_phu.CongThuc;
import He_thong_quan_ly.demo.Module.bang_phu.DonHang_detail;
import He_thong_quan_ly.demo.Module.bang_phu_id.donhang_detail_id;
import He_thong_quan_ly.demo.Repository.Admin.QuanlydonhangRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlysanphamRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.KhoBepRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.CongThucRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.DonHangDetailRepository;
import He_thong_quan_ly.demo.Service.Admin.order.OrderInventoryService;
import He_thong_quan_ly.demo.Service.Admin.order.OrderViewMapper;

@Service
@Transactional(readOnly = true)
public class QuanlydonhangService {

    private static final Logger log = LoggerFactory.getLogger(QuanlydonhangService.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final QuanlydonhangRepository donhangRepo;
    private final DonHangDetailRepository donhangDetailRepo;
    private final QuanlysanphamRepository sanphamRepo;
    private final QuanlynhanvienRepository nhanvienRepo;
    private final CongThucRepository congThucRepo;
    private final QuanlynguyenlieuRepository nguyenLieuRepo;
    private final KhoBepRepository khoBepRepository;
    private final OrderInventoryService orderInventoryService;
    private final OrderViewMapper orderViewMapper;

    public QuanlydonhangService(
            QuanlydonhangRepository donhangRepo,
            DonHangDetailRepository donhangDetailRepo,
            QuanlysanphamRepository sanphamRepo,
            QuanlynhanvienRepository nhanvienRepo,
            CongThucRepository congThucRepo,
            QuanlynguyenlieuRepository nguyenLieuRepo,
            KhoBepRepository khoBepRepository,
            OrderInventoryService orderInventoryService,
            OrderViewMapper orderViewMapper) {
        this.donhangRepo = donhangRepo;
        this.donhangDetailRepo = donhangDetailRepo;
        this.sanphamRepo = sanphamRepo;
        this.nhanvienRepo = nhanvienRepo;
        this.congThucRepo = congThucRepo;
        this.nguyenLieuRepo = nguyenLieuRepo;
        this.khoBepRepository = khoBepRepository;
        this.orderInventoryService = orderInventoryService;
        this.orderViewMapper = orderViewMapper;
    }

    @Transactional
    @CacheEvict(value = { "adminOrderRowsPage", "availableProductsByCoSo", "deliveryOrdersView" }, allEntries = true)
    public String taoDonHang(String username, OrderCreateRequest request) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Phiên đăng nhập không hợp lệ");
        }
        NhanVien_module nhanVien = nhanvienRepo.findByGmail(username)
                .orElseGet(() -> nhanvienRepo.findById(username)
                        .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại")));

        String cosoId = requireCoSoId(nhanVien);

        String donhangId = generateDonHangId();

        Map<String, Double> requiredByNguyenLieu = orderInventoryService.buildRequiredNguyenLieu(cosoId, request);
        orderInventoryService.validateNguyenLieuStock(cosoId, requiredByNguyenLieu);

        DonHang_module donhang = new DonHang_module();
        donhang.setDonhang_id(donhangId);
        donhang.setNhanVien(nhanVien);
        donhang.setTong_tien(calcTotal(request));
        donhang.setNgay_dat(LocalDateTime.now());
        donhang.setTrang_thai("Thành công");
        donhang.setPhan_loai("NV");
        donhang.setGhi_chu(request.getNote());

        donhangRepo.save(donhang);

        if (request.getItems() != null) {
            for (OrderItemRequest item : request.getItems()) {
                if (item.getId() == null || item.getId().isBlank()) {
                    continue;
                }
                SanPham_module sp = sanphamRepo.findById(item.getId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
                String size = item.getSize() == null || item.getSize().isBlank()
                        ? "M"
                        : item.getSize().trim().toUpperCase(Locale.ROOT);

                DonHang_detail detail = new DonHang_detail();
                detail.setId(new donhang_detail_id(donhangId, sp.getSanPhamId(), size));
                detail.setDonhang(donhang);
                detail.setSanPham(sp);
                detail.setSL(Math.max(1, item.getQty()));
                detail.setSize(size);
                detail.setSugar(item.getSugar());
                detail.setIce(item.getIce());
                detail.setMilk(item.getMilk());
                detail.setNote(item.getNote());
                donhangDetailRepo.save(detail);
            }
        }

        orderInventoryService.deductNguyenLieuStock(cosoId, requiredByNguyenLieu);

        return donhangId;
    }

    public void syncKhoBepForNhanVien(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        NhanVien_module nhanVien = nhanvienRepo.findByGmail(username)
                .orElseGet(() -> nhanvienRepo.findById(username)
                        .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại")));
        String cosoId = resolveCoSoId(nhanVien);
        if (cosoId == null) {
            return;
        }

        for (NguyenLieu_module nl : nguyenLieuRepo.findAll()) {
            if (nl == null || nl.getNguyenlieuId() == null) {
                continue;
            }
            if (!khoBepRepository.existsByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(
                    cosoId, nl.getNguyenlieuId())) {

                Khobep_module kb = new Khobep_module();
                kb.setNguyenlieuId(nl.getNguyenlieuId());
                kb.setCoSo(nhanVien.getCoSo());
                kb.setNguyenLieu(nl);
                kb.setDonVi(nl.getDonVi());
                kb.setSlTon(0);
                khoBepRepository.save(kb);
            }
        }
    }

    public String getCoSoIdByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        NhanVien_module nhanVien = nhanvienRepo.findByGmail(username)
                .orElseGet(() -> nhanvienRepo.findById(username)
                        .orElse(null));
        if (nhanVien == null || nhanVien.getCoSo() == null) {
            return null;
        }
        return nhanVien.getCoSo().getCosoId();
    }

    @Cacheable(value = "availableProductsByCoSo", key = "#username", condition = "#username != null && !#username.isBlank()")
    public List<SanPham_module> getAvailableProductsForNhanVien(String username, List<SanPham_module> products) {
        long startedAt = System.nanoTime();
        if (username == null || username.isBlank()) {
            return List.of();
        }
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        String cosoId = getCoSoIdByUsername(username);
        if (cosoId == null || cosoId.isBlank()) {
            return List.of();
        }

        List<String> productIds = products.stream()
                .map(SanPham_module::getSanPhamId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
        if (productIds.isEmpty()) {
            return List.of();
        }

        List<CongThuc> allCongThuc = congThucRepo.findBySanPhamIdIn(productIds);
        if (allCongThuc == null || allCongThuc.isEmpty()) {
            return List.of();
        }

        Map<String, List<CongThuc>> congThucByProduct = new HashMap<>();
        Set<String> nguyenLieuIds = new HashSet<>();
        for (CongThuc ct : allCongThuc) {
            if (ct == null || ct.getSanPhamId() == null || ct.getSanPhamId().isBlank()) {
                continue;
            }
            if (ct.getNguyenLieuId() == null || ct.getNguyenLieuId().isBlank()) {
                continue;
            }
            congThucByProduct.computeIfAbsent(ct.getSanPhamId(), k -> new ArrayList<>()).add(ct);
            nguyenLieuIds.add(ct.getNguyenLieuId());
        }

        if (nguyenLieuIds.isEmpty()) {
            return List.of();
        }

        Map<String, Double> stockByNguyenLieu = new HashMap<>();
        for (Khobep_module kb : khoBepRepository.findAllByCoSo_CosoIdAndNguyenLieu_NguyenlieuIdIn(
                cosoId,
                new ArrayList<>(nguyenLieuIds))) {
            if (kb == null || kb.getNguyenlieuId() == null || kb.getNguyenlieuId().isBlank()) {
                continue;
            }
            stockByNguyenLieu.put(kb.getNguyenlieuId(), kb.getSlTon());
        }

        List<SanPham_module> available = new ArrayList<>();
        for (SanPham_module sp : products) {
            if (sp == null || sp.getSanPhamId() == null || sp.getSanPhamId().isBlank()) {
                continue;
            }
            List<CongThuc> congThucs = congThucByProduct.get(sp.getSanPhamId());
            if (congThucs == null || congThucs.isEmpty()) {
                continue;
            }

            boolean ok = true;
            for (CongThuc ct : congThucs) {
                double required = java.util.Optional.ofNullable(ct.getSoLuong()).orElse(0.0d);
                if (required <= 0.0) {
                    continue;
                }
                double stock = stockByNguyenLieu.getOrDefault(ct.getNguyenLieuId(), 0.0);
                if (stock < required) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                available.add(sp);
            }
        }

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info(
                "[PERF] availableProducts cache-miss user={} coso={} inputProducts={} availableProducts={} elapsed={}ms",
                username,
                cosoId,
                products.size(),
                available.size(),
                elapsedMs);
        return available;
    }

    public boolean isAvailableForOrder(SanPham_module sanPham, String cosoId) {
        if (sanPham == null || sanPham.getSanPhamId() == null)
            return false;
        if (cosoId == null || cosoId.isBlank())
            return false;
        List<CongThuc> congThucs = congThucRepo.findBySanPhamId(sanPham.getSanPhamId());
        if (congThucs == null || congThucs.isEmpty())
            return false;
        for (CongThuc ct : congThucs) {
            if (ct.getNguyenLieuId() == null)
                continue;
            Double slObj = ct.getSoLuong();
            double required = slObj == null ? 0.0 : slObj;
            if (required <= 0) {
                continue;
            }
            Khobep_module khoBep = khoBepRepository
                    .findByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(cosoId, ct.getNguyenLieuId())
                    .orElse(null);
            double slTon = khoBep == null ? 0.0 : khoBep.getSlTon();
            if (slTon < required) {
                return false;
            }
        }
        return true;
    }

    public List<Map<String, String>> getOrderRows() {
        List<Map<String, String>> rows = new ArrayList<>();
        DateTimeFormatter dateFormatter = DATE_FORMATTER;
        DateTimeFormatter timeFormatter = TIME_FORMATTER;
        NumberFormat numberFormat = orderViewMapper.vnNumberFormat();

        for (DonHang_module dh : donhangRepo.findAll()) {
            rows.add(orderViewMapper.toAdminRow(dh, dateFormatter, timeFormatter, numberFormat));
        }

        return rows;
    }

    @Cacheable(value = "adminOrderRowsPage", key = "#page + '-' + #size")
    public Page<Map<String, String>> getOrderRowsPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        PageRequest pageable = PageRequest.of(pageIndex, pageSize);

        DateTimeFormatter dateFormatter = DATE_FORMATTER;
        DateTimeFormatter timeFormatter = TIME_FORMATTER;
        NumberFormat numberFormat = orderViewMapper.vnNumberFormat();

        Page<DonHang_module> ordersPage = donhangRepo.findAllForAdminPaged(pageable);
        List<Map<String, String>> content = ordersPage.getContent().stream()
                .map(dh -> orderViewMapper.toAdminRow(dh, dateFormatter, timeFormatter, numberFormat))
                .toList();

        return new PageImpl<>(content, pageable, ordersPage.getTotalElements());
    }

    public List<Map<String, String>> getHistoryRowsForNhanVien(String username) {
        if (username == null || username.isBlank()) {
            return new ArrayList<>();
        }
        NhanVien_module nhanVien = nhanvienRepo.findByGmail(username)
                .orElseGet(() -> nhanvienRepo.findById(username)
                        .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại")));

        DateTimeFormatter timeFormatter = TIME_FORMATTER;
        NumberFormat numberFormat = orderViewMapper.vnNumberFormat();
        List<Map<String, String>> rows = new ArrayList<>();

        for (DonHang_module dh : donhangRepo.findByNhanVienIdOrderByNgayDatDesc(
                nhanVien.getNhanvienId())) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", dh.getDonhang_id());
            row.put("typeBadge", dh.getPhan_loai() == null ? "NV" : dh.getPhan_loai());
            row.put("name", orderViewMapper.resolveName(dh));
            row.put("total", orderViewMapper.formatTotal(numberFormat, dh.getTong_tien(), " VND"));

            LocalDateTime ngayDat = dh.getNgay_dat();
            row.put("time", ngayDat == null ? "" : ngayDat.format(timeFormatter));

            String status = dh.getTrang_thai() == null ? "" : dh.getTrang_thai();
            row.put("status", status);
            row.put("statusValue", status);
            row.put("statusClass", orderViewMapper.resolveStatusClass(status));
            rows.add(row);
        }

        return rows;
    }

    public List<Map<String, String>> getPendingCustomerOrders(String username) {
        if (username == null || username.isBlank()) {
            return new ArrayList<>();
        }
        NhanVien_module nhanVien = nhanvienRepo.findByGmail(username)
                .orElseGet(() -> nhanvienRepo.findById(username).orElse(null));
        if (nhanVien == null || nhanVien.getNhanvienId() == null || nhanVien.getNhanvienId().isBlank()) {
            return new ArrayList<>();
        }

        DateTimeFormatter dateFormatter = DATE_FORMATTER;
        DateTimeFormatter timeFormatter = TIME_FORMATTER;
        NumberFormat numberFormat = orderViewMapper.vnNumberFormat();

        List<Map<String, String>> rows = new ArrayList<>();
        List<DonHang_module> pendingOrders = donhangRepo.findCustomerOrdersByStatusesAndNhanVienId(
                List.of("Chờ xác nhận", "Chờ xác thực"),
                nhanVien.getNhanvienId());

        // Keep stable ordering and avoid duplicates if legacy data overlaps.
        for (DonHang_module dh : new LinkedHashSet<>(pendingOrders)) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", dh.getDonhang_id());
            row.put("name", orderViewMapper.resolveName(dh));
            orderViewMapper.putCustomerPhone(row, dh);
            row.put("total", orderViewMapper.formatTotal(numberFormat, dh.getTong_tien(), " VND"));
            orderViewMapper.putDateTime(row, dh.getNgay_dat(), dateFormatter, timeFormatter);
            row.put("status", dh.getTrang_thai() == null ? "" : dh.getTrang_thai());
            rows.add(row);
        }

        return rows;
    }

    @Cacheable(value = "deliveryOrdersView", key = "'stats-' + #username", condition = "#username != null && !#username.isBlank()")
    public Map<String, Long> getDeliveryDashboardStats(String username) {
        NhanVien_module nhanVien = getNhanVienByUsername(username);
        String nhanvienId = nhanVien == null ? "" : nhanVien.getNhanvienId();

        long confirmed = donhangRepo.countConfirmedCustomerOrdersForDelivery();
        long delivering = (nhanvienId == null || nhanvienId.isBlank())
                ? 0L
                : donhangRepo.countByNhanVienIdAndStatus(nhanvienId, "Đang giao hàng");
        long completed = (nhanvienId == null || nhanvienId.isBlank())
                ? 0L
                : donhangRepo.countByNhanVienIdAndStatus(nhanvienId, "Đã hoàn thành");

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("confirmed", confirmed);
        stats.put("delivering", delivering);
        stats.put("completed", completed);
        return stats;
    }

    public Map<String, Long> getDeliveryStatusSummary(String username, int year, Integer month) {
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("delivering", 0L);
        summary.put("completed", 0L);
        summary.put("cancelled", 0L);
        summary.put("other", 0L);

        NhanVien_module nhanVien = getNhanVienByUsername(username);
        if (nhanVien == null || nhanVien.getNhanvienId() == null || nhanVien.getNhanvienId().isBlank()) {
            return summary;
        }

        int safeYear = year > 0 ? year : LocalDate.now().getYear();
        Integer safeMonth = (month != null && month >= 1 && month <= 12) ? month : null;

        List<Object[]> rows = donhangRepo.countStatusByNhanVienAndPeriod(
                nhanVien.getNhanvienId(),
                safeYear,
                safeMonth);

        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }
            String status = row[0] == null ? "" : String.valueOf(row[0]).trim();
            long value = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;

            if ("Đang giao hàng".equalsIgnoreCase(status)) {
                summary.put("delivering", summary.get("delivering") + value);
            } else if ("Đã hoàn thành".equalsIgnoreCase(status) || "Thành công".equalsIgnoreCase(status)) {
                summary.put("completed", summary.get("completed") + value);
            } else if ("Đã hủy".equalsIgnoreCase(status) || "Da huy".equalsIgnoreCase(status)) {
                summary.put("cancelled", summary.get("cancelled") + value);
            } else {
                summary.put("other", summary.get("other") + value);
            }
        }

        return summary;
    }

    @Cacheable(value = "deliveryOrdersView", key = "'confirmed'")
    public List<Map<String, String>> getConfirmedOrdersForDelivery() {
        DateTimeFormatter dateFormatter = DATE_FORMATTER;
        DateTimeFormatter timeFormatter = TIME_FORMATTER;
        NumberFormat numberFormat = orderViewMapper.vnNumberFormat();

        List<Map<String, String>> rows = new ArrayList<>();
        for (DonHang_module dh : donhangRepo.findConfirmedCustomerOrdersForDelivery()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", dh.getDonhang_id());
            row.put("name", orderViewMapper.resolveName(dh));
            orderViewMapper.putCustomerContact(row, dh);
            row.put("total", orderViewMapper.formatTotal(numberFormat, dh.getTong_tien(), " VND"));
            orderViewMapper.putDateTime(row, dh.getNgay_dat(), dateFormatter, timeFormatter);
            row.put("status", dh.getTrang_thai() == null ? "" : dh.getTrang_thai());
            rows.add(row);
        }
        return rows;
    }

    @Cacheable(value = "deliveryOrdersView", key = "'delivering-' + #username", condition = "#username != null && !#username.isBlank()")
    public List<Map<String, String>> getOrdersInDelivery(String username) {
        NhanVien_module nhanVien = getNhanVienByUsername(username);
        if (nhanVien == null) {
            return new ArrayList<>();
        }

        DateTimeFormatter dateFormatter = DATE_FORMATTER;
        DateTimeFormatter timeFormatter = TIME_FORMATTER;
        NumberFormat numberFormat = orderViewMapper.vnNumberFormat();

        List<Map<String, String>> rows = new ArrayList<>();
        for (DonHang_module dh : donhangRepo.findByNhanVienIdAndStatusOrderByNgayDatDesc(
                nhanVien.getNhanvienId(), "Đang giao hàng")) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", dh.getDonhang_id());
            row.put("name", orderViewMapper.resolveName(dh));
            orderViewMapper.putCustomerContact(row, dh);
            row.put("total", orderViewMapper.formatTotal(numberFormat, dh.getTong_tien(), " VND"));
            orderViewMapper.putDateTime(row, dh.getNgay_dat(), dateFormatter, timeFormatter);
            rows.add(row);
        }

        return rows;
    }

    @Cacheable(value = "deliveryOrdersView", key = "'cancelled-' + #username", condition = "#username != null && !#username.isBlank()")
    public List<Map<String, String>> getCancelledDeliveryOrders(String username) {
        NhanVien_module nhanVien = getNhanVienByUsername(username);
        if (nhanVien == null) {
            return new ArrayList<>();
        }

        DateTimeFormatter dateFormatter = DATE_FORMATTER;
        DateTimeFormatter timeFormatter = TIME_FORMATTER;
        NumberFormat numberFormat = orderViewMapper.vnNumberFormat();

        List<Map<String, String>> rows = new ArrayList<>();
        for (DonHang_module dh : donhangRepo.findByNhanVienIdAndStatusOrderByNgayDatDesc(
                nhanVien.getNhanvienId(), "Đã hủy")) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", dh.getDonhang_id());
            row.put("name", orderViewMapper.resolveName(dh));
            orderViewMapper.putCustomerContact(row, dh);
            row.put("total", orderViewMapper.formatTotal(numberFormat, dh.getTong_tien(), " VND"));
            orderViewMapper.putDateTime(row, dh.getNgay_dat(), dateFormatter, timeFormatter);
            row.put("reason", dh.getLy_do() == null ? "" : dh.getLy_do());
            rows.add(row);
        }
        return rows;
    }

    @Transactional
    @CacheEvict(value = { "adminOrderRowsPage", "availableProductsByCoSo", "deliveryOrdersView" }, allEntries = true)
    public void takeOrderForDelivery(String orderId, String username) {
        NhanVien_module nhanVien = getNhanVienByUsername(username);
        if (nhanVien == null) {
            throw new RuntimeException("Không tìm thấy nhân viên vận chuyển");
        }

        DonHang_module donhang = donhangRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (donhang.getTrang_thai() == null || !"Đã xác nhận".equalsIgnoreCase(donhang.getTrang_thai())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái đã xác nhận");
        }

        donhang.setNhanVien(nhanVien);
        donhang.setTrang_thai("Đang giao hàng");
        donhangRepo.save(donhang);
    }

    @Transactional
    @CacheEvict(value = { "adminOrderRowsPage", "availableProductsByCoSo", "deliveryOrdersView" }, allEntries = true)
    public void completeDeliveryOrder(String orderId, String username) {
        NhanVien_module nhanVien = getNhanVienByUsername(username);
        if (nhanVien == null) {
            throw new RuntimeException("Không tìm thấy nhân viên vận chuyển");
        }

        DonHang_module donhang = donhangRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (donhang.getNhanVien() == null
                || donhang.getNhanVien().getNhanvienId() == null
                || !donhang.getNhanVien().getNhanvienId().equals(nhanVien.getNhanvienId())) {
            throw new RuntimeException("Đơn hàng không thuộc nhân viên hiện tại");
        }

        if (donhang.getTrang_thai() == null || !"Đang giao hàng".equalsIgnoreCase(donhang.getTrang_thai())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái đang giao hàng");
        }

        long phiShip = java.util.Optional.ofNullable(donhang.getPhi_ship()).orElse(0L);
        long thuongGiaoHang = Math.round(phiShip * 2.0 / 3.0);
        donhang.setThuong_giao_hang(thuongGiaoHang);
        donhang.setTrang_thai("Đã hoàn thành");
        donhangRepo.save(donhang);
    }

    @Transactional
    @CacheEvict(value = { "adminOrderRowsPage", "availableProductsByCoSo", "deliveryOrdersView" }, allEntries = true)
    public void cancelDeliveryOrder(String orderId, String reason, String username) {
        NhanVien_module nhanVien = getNhanVienByUsername(username);
        if (nhanVien == null) {
            throw new RuntimeException("Không tìm thấy nhân viên vận chuyển");
        }

        DonHang_module donhang = donhangRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (donhang.getNhanVien() == null
                || donhang.getNhanVien().getNhanvienId() == null
                || !donhang.getNhanVien().getNhanvienId().equals(nhanVien.getNhanvienId())) {
            throw new RuntimeException("Đơn hàng không thuộc nhân viên hiện tại");
        }

        if (donhang.getTrang_thai() == null || !"Đang giao hàng".equalsIgnoreCase(donhang.getTrang_thai())) {
            throw new RuntimeException("Chỉ có thể hủy đơn đang giao hàng");
        }

        String lyDoHuy = (reason == null || reason.isBlank())
                ? "Hủy bởi nhân viên giao hàng"
                : reason.trim();
        huyDon(orderId, lyDoHuy);
    }

    public DonHang_module getDeliveryOrderDetail(String orderId, String username) {
        NhanVien_module nhanVien = getNhanVienByUsername(username);
        if (nhanVien == null) {
            throw new RuntimeException("Không tìm thấy nhân viên vận chuyển");
        }

        DonHang_module donhang = donhangRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (donhang.getNhanVien() == null
                || donhang.getNhanVien().getNhanvienId() == null
                || !donhang.getNhanVien().getNhanvienId().equals(nhanVien.getNhanvienId())) {
            throw new RuntimeException("Bạn chưa nhận đơn hàng này");
        }

        return donhang;
    }

    @Transactional
    @CacheEvict(value = { "adminOrderRowsPage", "availableProductsByCoSo", "deliveryOrdersView" }, allEntries = true)
    public void confirmCustomerOrder(String orderId, String username) {
        DonHang_module donhang = donhangRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        String currentStatus = donhang.getTrang_thai() == null ? "" : donhang.getTrang_thai();
        boolean pendingStatus = "Chờ xác nhận".equalsIgnoreCase(currentStatus)
                || "Chờ xác thực".equalsIgnoreCase(currentStatus);
        if (!pendingStatus) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ xác nhận");
        }

        NhanVien_module nhanVienXacNhan = null;
        if (username != null && !username.isBlank()) {
            nhanVienXacNhan = nhanvienRepo.findByGmail(username)
                    .orElseGet(() -> nhanvienRepo.findById(username).orElse(null));
        }
        if (nhanVienXacNhan == null) {
            nhanVienXacNhan = donhang.getNhanVien();
        }
        if (nhanVienXacNhan == null) {
            throw new RuntimeException("Không tìm thấy nhân viên xác nhận đơn hàng");
        }
        if (donhang.getNhanVien() != null
                && donhang.getNhanVien().getNhanvienId() != null
                && !donhang.getNhanVien().getNhanvienId().equals(nhanVienXacNhan.getNhanvienId())) {
            throw new RuntimeException("Đơn hàng này đã được gán cho nhân viên khác");
        }

        String cosoId = requireCoSoId(nhanVienXacNhan);
        Map<String, Double> requiredByNguyenLieu = orderInventoryService.buildRequiredNguyenLieuFromDetails(cosoId,
                orderId);
        orderInventoryService.validateNguyenLieuStock(cosoId, requiredByNguyenLieu);
        orderInventoryService.deductNguyenLieuStock(cosoId, requiredByNguyenLieu);

        donhang.setNhanVien(nhanVienXacNhan);
        donhang.setTrang_thai("Đã xác nhận");
        donhangRepo.save(donhang);
    }

    @Transactional
    @CacheEvict(value = { "adminOrderRowsPage", "availableProductsByCoSo", "deliveryOrdersView" }, allEntries = true)
    public DonHang_module huyDon(String orderId, String reason) {
        DonHang_module donhang = donhangRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        if (!"Đã hủy".equals(donhang.getTrang_thai())) {
            String cosoId = donhang.getNhanVien() != null
                    ? requireCoSoId(donhang.getNhanVien())
                    : null;
            if (cosoId != null) {
                Map<String, Double> requiredByNguyenLieu = orderInventoryService
                        .buildRequiredNguyenLieuFromDetails(cosoId, orderId);
                orderInventoryService.addBackNguyenLieuStock(cosoId, donhang.getNhanVien().getCoSo(),
                        requiredByNguyenLieu);
            }
        }
        donhang.setTrang_thai("Đã hủy");
        donhang.setLy_do(reason);
        return donhangRepo.save(donhang);
    }

    private long calcTotal(OrderCreateRequest request) {
        if (request.getItems() == null)
            return 0L;
        long total = 0L;
        for (OrderItemRequest item : request.getItems()) {
            total += item.getPrice() * (long) Math.max(1, item.getQty());
        }
        return total;
    }

    private String resolveCoSoId(NhanVien_module nhanVien) {
        if (nhanVien == null || nhanVien.getCoSo() == null
                || nhanVien.getCoSo().getCosoId() == null
                || nhanVien.getCoSo().getCosoId().isBlank()) {
            return null;
        }
        return nhanVien.getCoSo().getCosoId();
    }

    private String requireCoSoId(NhanVien_module nhanVien) {
        String cosoId = resolveCoSoId(nhanVien);
        if (cosoId == null) {
            throw new RuntimeException("Nhân viên chưa được gán cơ sở");
        }
        return cosoId;
    }

    private NhanVien_module getNhanVienByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return nhanvienRepo.findByGmail(username)
                .orElseGet(() -> nhanvienRepo.findById(username).orElse(null));
    }

    private String generateDonHangId() {
        List<String> ids = donhangRepo.findAllIdDesc();

        if (ids.isEmpty()) {
            return "DH001";
        }

        String last = ids.get(0);

        try {
            int number = Integer.parseInt(last.substring(2));
            return String.format("DH%03d", number + 1);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return "DH001";
        }
    }
}
