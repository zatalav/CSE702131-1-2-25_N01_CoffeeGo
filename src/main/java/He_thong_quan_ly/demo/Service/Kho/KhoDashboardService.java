package He_thong_quan_ly.demo.Service.Kho;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.NhanVienKho.NhapKho_module;
import He_thong_quan_ly.demo.Module.NhanVienKho.XuatKho_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.NhapKhoRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.XuatKhoRepository;

@Service
@Transactional(readOnly = true)
public class KhoDashboardService {

        private final QuanlynguyenlieuRepository nguyenlieuRepository;
        private final NhapKhoRepository nhapKhoRepository;
        private final XuatKhoRepository xuatKhoRepository;

        public KhoDashboardService(
                        QuanlynguyenlieuRepository nguyenlieuRepository,
                        NhapKhoRepository nhapKhoRepository,
                        XuatKhoRepository xuatKhoRepository) {
                this.nguyenlieuRepository = nguyenlieuRepository;
                this.nhapKhoRepository = nhapKhoRepository;
                this.xuatKhoRepository = xuatKhoRepository;
        }

        @Cacheable("khoDashboardData")
        public Map<String, Object> buildDashboardData() {
                Map<String, Object> data = new LinkedHashMap<>();
                LocalDate today = LocalDate.now();
                LocalDate sevenDaysAgo = today.minusDays(6);

                int totalStock = Math.toIntExact(nguyenlieuRepository.sumSlTon());
                long lowStockCount = nguyenlieuRepository.countLowStock(10);
                long expirySoonCount = nguyenlieuRepository.countExpiryBetween(today, today.plusDays(7));

                List<NhapKho_module> recentNhap = nhapKhoRepository.findRecent(PageRequest.of(0, 5));
                List<XuatKho_module> recentXuat = xuatKhoRepository.findRecent(PageRequest.of(0, 5));
                long pendingNhapCount = nhapKhoRepository.countFromDate(today.minusDays(7));

                List<NguyenLieu_module> lowStockItems = nguyenlieuRepository.findTopLowStock(10, PageRequest.of(0, 4));
                List<NguyenLieu_module> topNguyenLieu = nguyenlieuRepository
                                .findTopByEarliestExpiry(PageRequest.of(0, 4));

                Map<LocalDate, Long> nhapByDate = nhapKhoRepository.countByDateRange(sevenDaysAgo, today).stream()
                                .collect(Collectors.toMap(NhapKhoRepository.DateCountView::getNgay,
                                                NhapKhoRepository.DateCountView::getTotal));

                Map<LocalDate, Long> xuatByDate = xuatKhoRepository.countByDateRange(sevenDaysAgo, today).stream()
                                .collect(Collectors.toMap(XuatKhoRepository.DateCountView::getNgay,
                                                XuatKhoRepository.DateCountView::getTotal));

                List<String> labels = new ArrayList<>();
                List<Long> nhapSeries = new ArrayList<>();
                List<Long> xuatSeries = new ArrayList<>();
                for (int i = 6; i >= 0; i--) {
                        LocalDate day = today.minusDays(i);
                        labels.add(day.toString());
                        nhapSeries.add(nhapByDate.getOrDefault(day, 0L));
                        xuatSeries.add(xuatByDate.getOrDefault(day, 0L));
                }

                data.put("totalStock", totalStock);
                data.put("lowStockCount", lowStockCount);
                data.put("expirySoonCount", expirySoonCount);
                data.put("pendingNhapCount", pendingNhapCount);
                data.put("lowStockItems", lowStockItems);
                data.put("topNguyenLieu", topNguyenLieu);
                data.put("recentNhap", recentNhap);
                data.put("recentXuat", recentXuat);
                data.put("chartLabels", labels);
                data.put("chartNhap", nhapSeries);
                data.put("chartXuat", xuatSeries);

                return data;
        }
}
