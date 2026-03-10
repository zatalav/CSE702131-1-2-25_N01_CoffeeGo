package He_thong_quan_ly.demo.Service.Kho;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
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

                List<NguyenLieu_module> nguyenLieuList = nguyenlieuRepository.findAll();
                int totalStock = nguyenLieuList.stream().mapToInt(NguyenLieu_module::getSlTon).sum();
                long lowStockCount = nguyenLieuList.stream().filter(nl -> nl.getSlTon() > 0 && nl.getSlTon() < 10)
                                .count();
                long expirySoonCount = nguyenLieuList.stream()
                                .filter(nl -> nl.getHanSuDung() != null)
                                .filter(nl -> !nl.getHanSuDung().isBefore(LocalDate.now()))
                                .filter(nl -> !nl.getHanSuDung().isAfter(LocalDate.now().plusDays(7)))
                                .count();

                List<NhapKho_module> nhapList = nhapKhoRepository.findAllOrderByNgayNhapDesc();
                List<XuatKho_module> xuatList = xuatKhoRepository.findAllOrderByNgayXuatDesc();

                long pendingNhapCount = nhapList.stream()
                                .filter(nk -> nk.getNgayNhap() != null
                                                && !nk.getNgayNhap().isBefore(LocalDate.now().minusDays(7)))
                                .count();

                List<NguyenLieu_module> lowStockItems = nguyenLieuList.stream()
                                .filter(nl -> nl.getSlTon() <= 10)
                                .sorted(Comparator.comparingInt(NguyenLieu_module::getSlTon))
                                .limit(4)
                                .toList();

                List<NguyenLieu_module> topNguyenLieu = nguyenLieuList.stream()
                                .sorted(Comparator.comparing(NguyenLieu_module::getHanSuDung,
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                .limit(4)
                                .toList();

                List<NhapKho_module> recentNhap = nhapList.stream().limit(5).toList();
                List<XuatKho_module> recentXuat = xuatList.stream().limit(5).toList();

                Map<LocalDate, Long> nhapByDate = nhapList.stream()
                                .filter(nk -> nk.getNgayNhap() != null)
                                .collect(Collectors.groupingBy(NhapKho_module::getNgayNhap, Collectors.counting()));
                Map<LocalDate, Long> xuatByDate = xuatList.stream()
                                .filter(xk -> xk.getNgayXuat() != null)
                                .collect(Collectors.groupingBy(XuatKho_module::getNgayXuat, Collectors.counting()));

                List<String> labels = new ArrayList<>();
                List<Long> nhapSeries = new ArrayList<>();
                List<Long> xuatSeries = new ArrayList<>();
                for (int i = 6; i >= 0; i--) {
                        LocalDate day = LocalDate.now().minusDays(i);
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
