package He_thong_quan_ly.demo.Service.Kho;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.CoSo_module;
import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.NhanVienKho.Khobep_module;
import He_thong_quan_ly.demo.Module.NhanVienKho.XuatKhoChiTiet;
import He_thong_quan_ly.demo.Module.NhanVienKho.XuatKhoChiTietId;
import He_thong_quan_ly.demo.Module.NhanVienKho.XuatKho_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlycosoRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.KhoBepRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.XuatKhoChiTietRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.XuatKhoRepository;

@Service
@Transactional(readOnly = true)
public class XuatKhoService {
    private final XuatKhoRepository xuatKhoRepository;
    private final QuanlynhanvienRepository nhanvienRepository;
    private final QuanlynguyenlieuRepository nguyenlieuRepository;
    private final KhoBepRepository khoBepRepository;
    private final QuanlycosoRepository cosoRepository;
    private final XuatKhoChiTietRepository xuatKhoChiTietRepository;

    public XuatKhoService(
            XuatKhoRepository xuatKhoRepository,
            QuanlynhanvienRepository nhanvienRepository,
            QuanlynguyenlieuRepository nguyenlieuRepository,
            KhoBepRepository khoBepRepository,
            QuanlycosoRepository cosoRepository,
            XuatKhoChiTietRepository xuatKhoChiTietRepository) {
        this.xuatKhoRepository = xuatKhoRepository;
        this.nhanvienRepository = nhanvienRepository;
        this.nguyenlieuRepository = nguyenlieuRepository;
        this.khoBepRepository = khoBepRepository;
        this.cosoRepository = cosoRepository;
        this.xuatKhoChiTietRepository = xuatKhoChiTietRepository;
    }

    @Cacheable("warehouseXuatKhoList")
    public List<XuatKho_module> findAll() {
        return xuatKhoRepository.findAllOrderByNgayXuatDesc();
    }

    @Cacheable(value = "warehouseTonKhoPage", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #keyword")
    public Page<NguyenLieu_module> findTonKhoPaged(Pageable pageable, String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return nguyenlieuRepository.findPagedForKho(normalizedKeyword, pageable);
    }

    @Cacheable(value = "warehouseXuatKhoPage", key = "#page + '-' + #size + '-' + #keyword")
    public Page<XuatKho_module> findPaged(int page, int size, String keyword) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        var pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "ngayXuat"));
        return xuatKhoRepository.findPaged(normalizedKeyword, pageable);
    }

    public List<XuatKho_module> findAllByIds(List<String> ids) {
        return xuatKhoRepository.findAllById(ids);
    }

    @Transactional
    @CacheEvict(value = { "warehouseRowsAll", "warehouseRowsPage", "khoDashboardData",
            "warehouseXuatKhoList", "warehouseXuatKhoPage", "warehouseTonKhoPage",
            "availableProductsByCoSo" }, allEntries = true)
    public void capNhatPhieuXuat(String maPhieu, LocalDate ngayXuat, String ghiChu) {
        if (maPhieu == null || maPhieu.isBlank()) {
            throw new RuntimeException("Mã phiếu không hợp lệ");
        }
        XuatKho_module xuatKho = xuatKhoRepository.findById(maPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất kho"));

        if (ngayXuat != null) {
            xuatKho.setNgayXuat(ngayXuat);
        }
        xuatKho.setGhiChu(ghiChu);
        xuatKhoRepository.save(xuatKho);
    }

    @Transactional
    @CacheEvict(value = { "warehouseRowsAll", "warehouseRowsPage", "khoDashboardData",
            "warehouseXuatKhoList", "warehouseXuatKhoPage", "warehouseTonKhoPage",
            "availableProductsByCoSo" }, allEntries = true)
    public void xoaPhieuXuat(String maPhieu) {
        if (maPhieu == null || maPhieu.isBlank()) {
            throw new RuntimeException("Mã phiếu không hợp lệ");
        }

        var details = xuatKhoChiTietRepository.findByXuatKhoIds(java.util.List.of(maPhieu));
        for (var detail : details) {
            var nguyenLieu = detail.getNguyenLieu();
            int soLuong = detail.getSoLuong();
            if (nguyenLieu != null && soLuong > 0) {
                int newTon = nguyenLieu.getSlTon() + soLuong;
                nguyenLieu.setSlTon(newTon);
                nguyenLieu.setTrangThai(computeTrangThai(newTon));
                nguyenlieuRepository.save(nguyenLieu);
            }

            String cosoId = detail.getCosoId();
            if (cosoId != null && !cosoId.isBlank() && nguyenLieu != null) {
                var khoBepOpt = khoBepRepository.findByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(
                        cosoId,
                        nguyenLieu.getNguyenlieuId());
                if (khoBepOpt.isPresent()) {
                    var khoBep = khoBepOpt.get();
                    double slTon = Math.max(0.0, khoBep.getSlTon() - soLuong);
                    khoBep.setSlTon(slTon);
                    khoBepRepository.save(khoBep);
                }
            }
        }

        xuatKhoChiTietRepository.deleteAll(details);
        xuatKhoRepository.deleteById(maPhieu);
    }

    public String generateNextId() {
        String lastId = xuatKhoRepository.findLatestId();
        if (lastId == null || lastId.isBlank()) {
            return "PX-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        String digits = lastId.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return "PX-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        try {
            long number = Long.parseLong(digits);
            String next = String.valueOf(number + 1);
            if (digits.length() > next.length()) {
                next = "0".repeat(digits.length() - next.length()) + next;
            }
            return "PX-" + next;
        } catch (NumberFormatException ex) {
            return "PX-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
    }

    @Transactional
    @CacheEvict(value = { "warehouseRowsAll", "warehouseRowsPage", "khoDashboardData",
            "warehouseXuatKhoList", "warehouseXuatKhoPage", "warehouseTonKhoPage",
            "availableProductsByCoSo" }, allEntries = true)
    public void taoPhieuXuat(
            String username,
            String cosoId,
            LocalDate ngayXuat,
            String ghiChu,
            List<String> nguyenLieuIds,
            List<Integer> soLuongs,
            List<Long> giaNhaps) {
        var nhanVien = nhanvienRepository.findByGmail(username)
                .or(() -> nhanvienRepository.findById(username))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        CoSo_module coSo = cosoRepository.findById(cosoId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cơ sở"));

        if (nguyenLieuIds == null || nguyenLieuIds.isEmpty()) {
            throw new RuntimeException("Danh sách nguyên liệu trống");
        }

        java.util.Map<String, Integer> tongTheoNguyenLieu = new java.util.LinkedHashMap<>();
        int size = nguyenLieuIds.size();
        for (int i = 0; i < size; i++) {
            String nlId = nguyenLieuIds.get(i);
            if (nlId == null || nlId.isBlank()) {
                continue;
            }
            int soLuong = soLuongs != null && soLuongs.size() > i && soLuongs.get(i) != null
                    ? Math.max(0, soLuongs.get(i))
                    : 0;
            tongTheoNguyenLieu.put(nlId, tongTheoNguyenLieu.getOrDefault(nlId, 0) + soLuong);
        }

        if (tongTheoNguyenLieu.isEmpty()) {
            throw new RuntimeException("Số lượng nguyên liệu phải lớn hơn 0");
        }

        java.util.Map<String, NguyenLieu_module> cacheNguyenLieu = new java.util.HashMap<>();
        for (var entry : tongTheoNguyenLieu.entrySet()) {
            String nlId = entry.getKey();
            int soLuong = entry.getValue();
            if (soLuong <= 0) {
                continue;
            }

            NguyenLieu_module nguyenLieu = nguyenlieuRepository.findById(nlId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));

            int slTonHienTai = nguyenLieu.getSlTon();
            if (slTonHienTai < soLuong) {
                throw new RuntimeException("Nguyên liệu không đủ: " + nguyenLieu.getTenNguyenLieu());
            }
            cacheNguyenLieu.put(nlId, nguyenLieu);
        }

        String maPhieu = generateNextId();

        XuatKho_module xuatKho = new XuatKho_module();
        xuatKho.setXuatkhoId(maPhieu);
        xuatKho.setNhanvien(nhanVien);
        xuatKho.setCosoId(coSo.getCosoId());
        xuatKho.setNgayXuat(ngayXuat == null ? LocalDate.now() : ngayXuat);
        xuatKho.setGhiChu(ghiChu);
        xuatKhoRepository.save(xuatKho);

        for (var entry : tongTheoNguyenLieu.entrySet()) {
            String nlId = entry.getKey();
            int soLuong = entry.getValue();
            if (soLuong <= 0) {
                continue;
            }

            NguyenLieu_module nguyenLieu = cacheNguyenLieu.get(nlId);
            if (nguyenLieu == null) {
                nguyenLieu = nguyenlieuRepository.findById(nlId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));
            }

            int slTonHienTai = nguyenLieu.getSlTon();
            nguyenLieu.setSlTon(slTonHienTai - soLuong);
            nguyenLieu.setTrangThai(computeTrangThai(nguyenLieu.getSlTon()));

            XuatKhoChiTiet detail = new XuatKhoChiTiet();
            detail.setId(new XuatKhoChiTietId(maPhieu, nlId));
            detail.setXuatKho(xuatKho);
            detail.setNguyenLieu(nguyenLieu);
            detail.setSoLuong(soLuong);
            detail.setCosoId(coSo.getCosoId());
            xuatKhoChiTietRepository.save(detail);

            Khobep_module khoBep = khoBepRepository
                    .findByCoSo_CosoIdAndNguyenLieu_NguyenlieuId(coSo.getCosoId(), nlId)
                    .orElseGet(() -> khoBepRepository.findFirstByNguyenLieu_NguyenlieuId(nlId).orElse(null));

            if (khoBep == null) {
                khoBep = new Khobep_module();
                khoBep.setNguyenlieuId(nlId);
                khoBep.setCoSo(coSo);
                khoBep.setNguyenLieu(nguyenLieu);
                khoBep.setDonVi(nguyenLieu.getDonVi());
                khoBep.setSlTon(0);
            }

            if (khoBep.getCoSo() == null) {
                khoBep.setCoSo(coSo);
            }
            if (khoBep.getNguyenLieu() == null) {
                khoBep.setNguyenLieu(nguyenLieu);
            }
            khoBep.setSlTon(khoBep.getSlTon() + soLuong);
            if (khoBep.getDonVi() == null || khoBep.getDonVi().isBlank()) {
                khoBep.setDonVi(nguyenLieu.getDonVi());
            }

            nguyenlieuRepository.save(nguyenLieu);
            khoBepRepository.save(khoBep);
        }

    }

    private String computeTrangThai(int slTon) {
        if (slTon <= 0)
            return "Hết hàng";
        if (slTon < 10)
            return "Sắp hết hàng";
        return "Còn hàng";
    }
}
