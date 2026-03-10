package He_thong_quan_ly.demo.Service.Admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Repository.NhanVienKho.XuatKhoChiTietRepository;
import He_thong_quan_ly.demo.Repository.NhanVienKho.NhapKhoDetailRepository;

@Service
@Transactional(readOnly = true)
public class QuanlykhoService {

    private final NhapKhoDetailRepository nhapKhoDetailRepository;
    private final XuatKhoChiTietRepository xuatKhoChiTietRepository;

    public QuanlykhoService(
            NhapKhoDetailRepository nhapKhoDetailRepository,
            XuatKhoChiTietRepository xuatKhoChiTietRepository) {
        this.nhapKhoDetailRepository = nhapKhoDetailRepository;
        this.xuatKhoChiTietRepository = xuatKhoChiTietRepository;
    }

    @Cacheable("warehouseRowsAll")
    public List<KhoRow> getKhoRows() {
        List<KhoRow> rows = new ArrayList<>();

        nhapKhoDetailRepository.findAll().forEach(detail -> {
            var nk = detail.getNhapkho();
            String maPhieu = nk == null ? "" : nk.getNhapkhoId();
            String nv = nk == null || nk.getNhanvien() == null ? "" : nk.getNhanvien().getTenNv();
            String ghiChu = nk == null ? "" : nk.getGhiChu();
            String tenNl = detail.getNguyenlieu() == null ? "" : detail.getNguyenlieu().getTenNguyenLieu();
            Integer soLuong = detail.getSoLuong();
            String donVi = detail.getDonVi();
            if (donVi == null || donVi.isBlank()) {
                donVi = detail.getNguyenlieu() == null ? "" : detail.getNguyenlieu().getDonVi();
            }
            rows.add(new KhoRow(maPhieu, "Nhập Kho", nv, tenNl, soLuong, donVi, ghiChu));
        });

        xuatKhoChiTietRepository.findAll().forEach(detail -> {
            var xk = detail.getXuatKho();
            String maPhieu = xk == null ? "" : xk.getXuatkhoId();
            String nv = xk == null || xk.getNhanvien() == null ? "" : xk.getNhanvien().getTenNv();
            String ghiChu = xk == null ? "" : xk.getGhiChu();
            String tenNl = detail.getNguyenLieu() == null ? "" : detail.getNguyenLieu().getTenNguyenLieu();
            Integer soLuong = detail.getSoLuong();
            String donVi = detail.getNguyenLieu() == null ? "" : detail.getNguyenLieu().getDonVi();
            rows.add(new KhoRow(maPhieu, "Xuất Kho", nv, tenNl, soLuong, donVi, ghiChu));
        });

        rows.sort(Comparator.comparing(KhoRow::getMaPhieu, String.CASE_INSENSITIVE_ORDER).reversed());
        return rows;
    }

    @Cacheable(value = "warehouseRowsPage", key = "#page + '-' + #size")
    public Page<KhoRow> getKhoRowsPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        PageRequest pageable = PageRequest.of(pageIndex, pageSize);

        List<KhoRow> allRows = getKhoRows();
        int start = Math.min((int) pageable.getOffset(), allRows.size());
        int end = Math.min(start + pageable.getPageSize(), allRows.size());
        List<KhoRow> content = allRows.subList(start, end);
        return new PageImpl<>(content, pageable, allRows.size());
    }
}
