package He_thong_quan_ly.demo.Service.Kho;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Module.NhanVienKho.NhapKho_module;
import He_thong_quan_ly.demo.Module.bang_phu.NhapKho_detail;
import He_thong_quan_ly.demo.Module.bang_phu_id.NhapKho_detail_id;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhacungcapRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.NhapKhoDetailRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.NhapKhoRepository;

@Service
@Transactional(readOnly = true)
public class NhapKhoService {

    private final NhapKhoRepository nhapKhoRepository;
    private final NhapKhoDetailRepository nhapKhoDetailRepository;
    private final QuanlynhanvienRepository nhanvienRepository;
    private final QuanlynhacungcapRepository nhacungcapRepository;
    private final QuanlynguyenlieuRepository nguyenlieuRepository;

    public NhapKhoService(
            NhapKhoRepository nhapKhoRepository,
            NhapKhoDetailRepository nhapKhoDetailRepository,
            QuanlynhanvienRepository nhanvienRepository,
            QuanlynhacungcapRepository nhacungcapRepository,
            QuanlynguyenlieuRepository nguyenlieuRepository) {
        this.nhapKhoRepository = nhapKhoRepository;
        this.nhapKhoDetailRepository = nhapKhoDetailRepository;
        this.nhanvienRepository = nhanvienRepository;
        this.nhacungcapRepository = nhacungcapRepository;
        this.nguyenlieuRepository = nguyenlieuRepository;
    }

    @Cacheable("warehouseNhapKhoList")
    public List<NhapKho_module> findAll() {
        return nhapKhoRepository.findAllOrderByNgayNhapDesc();
    }

    @Cacheable(value = "warehouseNhapKhoPage", key = "#page + '-' + #size + '-' + #keyword")
    public Page<NhapKho_module> findPaged(int page, int size, String keyword) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        var pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "ngayNhap"));
        return nhapKhoRepository.findPaged(normalizedKeyword, pageable);
    }

    public List<NhapKho_module> findByIds(List<String> ids) {
        return nhapKhoRepository.findAllById(ids);
    }

    @Transactional
    @CacheEvict(value = { "warehouseRowsAll", "warehouseRowsPage", "khoDashboardData",
            "warehouseNhapKhoList", "warehouseNhapKhoPage", "warehouseTonKhoPage",
            "availableProductsByCoSo" }, allEntries = true)
    public void capNhatPhieuNhap(String maPhieu, LocalDate ngayNhap, String ghiChu) {
        if (maPhieu == null || maPhieu.isBlank()) {
            throw new RuntimeException("Mã phiếu không hợp lệ");
        }
        NhapKho_module nhapKho = nhapKhoRepository.findById(maPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập kho"));

        if (ngayNhap != null) {
            nhapKho.setNgayNhap(ngayNhap);
        }
        nhapKho.setGhiChu(ghiChu);
        nhapKhoRepository.save(nhapKho);
    }

    @Transactional
    @CacheEvict(value = { "warehouseRowsAll", "warehouseRowsPage", "khoDashboardData",
            "warehouseNhapKhoList", "warehouseNhapKhoPage", "warehouseTonKhoPage",
            "availableProductsByCoSo" }, allEntries = true)
    public void xoaPhieuNhap(String maPhieu) {
        if (maPhieu == null || maPhieu.isBlank()) {
            throw new RuntimeException("Mã phiếu không hợp lệ");
        }
        var details = nhapKhoDetailRepository.findByMaPhieu(maPhieu);
        for (var detail : details) {
            var nguyenLieu = detail.getNguyenlieu();
            Integer soLuong = detail.getSoLuong();
            int sl = soLuong == null ? 0 : soLuong;
            if (nguyenLieu != null && sl > 0) {
                int newTon = Math.max(0, nguyenLieu.getSlTon() - sl);
                nguyenLieu.setSlTon(newTon);
                nguyenLieu.setTrangThai(computeTrangThai(newTon));
                nguyenlieuRepository.save(nguyenLieu);
            }
        }
        nhapKhoDetailRepository.deleteAll(details);
        nhapKhoRepository.deleteById(maPhieu);
    }

    public String generateNextId() {
        String lastId = nhapKhoRepository.findLatestId();
        if (lastId == null || lastId.isBlank()) {
            return "PN-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        String digits = lastId.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return "PN-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        try {
            long number = Long.parseLong(digits);
            String next = String.valueOf(number + 1);
            if (digits.length() > next.length()) {
                next = "0".repeat(digits.length() - next.length()) + next;
            }
            return "PN-" + next;
        } catch (NumberFormatException ex) {
            return "PN-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
    }

    @Transactional
    @CacheEvict(value = { "warehouseRowsAll", "warehouseRowsPage", "khoDashboardData",
            "warehouseNhapKhoList", "warehouseNhapKhoPage", "warehouseTonKhoPage",
            "availableProductsByCoSo" }, allEntries = true)
    public void taoPhieuNhap(
            String username,
            String maPhieuInput,
            LocalDate ngayNhap,
            String ghiChu,
            List<String> nguyenLieuIds,
            List<String> nhaCungCapIds,
            List<String> donVis,
            List<Integer> soLuongs,
            List<Long> giaNhaps,
            List<LocalDate> hanSuDungs) {

        var nhanVien = nhanvienRepository.findByGmail(username)
                .or(() -> nhanvienRepository.findById(username))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        String maPhieu = generateNextId();

        NhapKho_module nhapKho = new NhapKho_module();
        nhapKho.setNhapkhoId(maPhieu);
        nhapKho.setNhanvien(nhanVien);
        nhapKho.setNgayNhap(ngayNhap == null ? LocalDate.now() : ngayNhap);
        nhapKho.setGhiChu(ghiChu);
        nhapKho.setTongTien(0L);

        nhapKhoRepository.save(nhapKho);

        if (nguyenLieuIds == null || nguyenLieuIds.isEmpty()) {
            throw new RuntimeException("Danh sách nguyên liệu trống");
        }

        long total = 0L;
        int size = nguyenLieuIds.size();
        for (int i = 0; i < size; i++) {
            String nlId = nguyenLieuIds.get(i);
            if (nlId == null || nlId.isBlank()) {
                continue;
            }
            NguyenLieu_module nguyenLieu = nguyenlieuRepository.findById(nlId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));

            String nccId = nhaCungCapIds != null && nhaCungCapIds.size() > i ? nhaCungCapIds.get(i) : null;
            var nhaCungCap = nccId == null || nccId.isBlank()
                    ? null
                    : nhacungcapRepository.findById(nccId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));

            Integer slValue = soLuongs != null && soLuongs.size() > i ? soLuongs.get(i) : null;
            Long giaNhapValue = giaNhaps != null && giaNhaps.size() > i ? giaNhaps.get(i) : null;
            LocalDate hanSuDung = hanSuDungs != null && hanSuDungs.size() > i ? hanSuDungs.get(i) : null;
            String donVi = donVis != null && donVis.size() > i ? donVis.get(i) : null;

            int sl = slValue == null ? 0 : slValue;
            long giaNhap = giaNhapValue == null ? 0L : giaNhapValue;

            if (sl <= 0) {
                continue;
            }

            long lineTotal = giaNhap * sl;
            total += lineTotal;

            NhapKho_detail detail = new NhapKho_detail();
            NhapKho_detail_id detailId = new NhapKho_detail_id(maPhieu, nguyenLieu.getNguyenlieuId());
            detail.setId(detailId);
            detail.setNhapkho(nhapKho);
            detail.setNguyenlieu(nguyenLieu);
            detail.setNhaCungCap(nhaCungCap);
            detail.setSoLuong(sl);
            detail.setDonVi(donVi);
            nhapKhoDetailRepository.save(detail);

            nguyenLieu.setSlTon(nguyenLieu.getSlTon() + sl);
            nguyenLieu.setTrangThai(computeTrangThai(nguyenLieu.getSlTon()));
            if (giaNhap > 0) {
                nguyenLieu.setGiaNhap(giaNhap);
            }
            if (donVi != null && !donVi.isBlank()) {
                nguyenLieu.setDonVi(donVi);
            }
            if (hanSuDung != null) {
                nguyenLieu.setHanSuDung(hanSuDung);
            }
            nguyenlieuRepository.save(nguyenLieu);
        }

        nhapKho.setTongTien(total);
        nhapKhoRepository.save(nhapKho);
    }

    private String computeTrangThai(int slTon) {
        if (slTon <= 0)
            return "Hết hàng";
        if (slTon < 10)
            return "Sắp hết hàng";
        return "Còn hàng";
    }
}
