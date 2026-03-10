package He_thong_quan_ly.demo.Service.Admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.CoSo_module;
import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlycosoRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.Util.VnIdentityValidator;

@Service
@Transactional(readOnly = true)
public class QuanlynhanvienService {

    @Autowired
    private QuanlynhanvienRepository nhanvienRepo;

    @Autowired
    private QuanlycosoRepository cosoRepo;

    @Cacheable("allEmployees")
    public List<NhanVien_module> getAll() {
        return nhanvienRepo.findAll();
    }

    @Cacheable(value = "adminEmployeePage", key = "#page + '-' + #size")
    public Page<NhanVien_module> getAllPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        return nhanvienRepo.findAllForAdminPaged(PageRequest.of(pageIndex, pageSize));
    }

    @Cacheable("allBranches")
    public List<CoSo_module> getAllCoSo() {
        return cosoRepo.findAll();
    }

    public NhanVien_module findById(String id) {
        return nhanvienRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
    }

    @CacheEvict(value = { "allEmployees", "adminEmployeePage" }, allEntries = true)
    @Transactional
    public void themNhanVien(NhanVien_module nv) {
        String gmail = nv.getGmail() == null ? "" : nv.getGmail().trim();
        if (gmail.isBlank()) {
            throw new RuntimeException("Email nhân viên không được để trống");
        }
        if (nhanvienRepo.existsByGmailIgnoreCase(gmail)) {
            throw new RuntimeException("Email nhân viên đã tồn tại");
        }
        nv.setCccd(VnIdentityValidator.normalizeCccdOrNull(nv.getCccd()));
        nv.setSdt(VnIdentityValidator.normalizeVietnamPhoneOrNull(nv.getSdt()));
        nv.setGmail(gmail);
        nv.setNhanvienId(tuDongSinhMa());
        nhanvienRepo.save(nv);
    }

    @CacheEvict(value = { "allEmployees", "adminEmployeePage" }, allEntries = true)
    @Transactional
    public void capNhatNhanVien(NhanVien_module nv) {
        NhanVien_module old = findById(nv.getNhanvienId());
        String gmail = nv.getGmail() == null ? "" : nv.getGmail().trim();
        if (gmail.isBlank()) {
            throw new RuntimeException("Email nhân viên không được để trống");
        }
        if (nhanvienRepo.existsByGmailIgnoreCaseAndNhanvienIdNot(gmail, old.getNhanvienId())) {
            throw new RuntimeException("Email nhân viên đã tồn tại");
        }
        old.setTenNv(nv.getTenNv());
        old.setChucVu(nv.getChucVu());
        old.setGioiTinh(nv.getGioiTinh());
        old.setNgaySinh(nv.getNgaySinh());
        old.setCccd(VnIdentityValidator.normalizeCccdOrNull(nv.getCccd()));
        old.setSdt(VnIdentityValidator.normalizeVietnamPhoneOrNull(nv.getSdt()));
        old.setGmail(gmail);
        old.setPassword(nv.getPassword());
        old.setDiaChi(nv.getDiaChi());
        if (nv.getImgNv() != null && !nv.getImgNv().isBlank()) {
            old.setImgNv(nv.getImgNv());
        }
        old.setCoSo(nv.getCoSo());
        nhanvienRepo.save(old);
    }

    public void ganCoSo(NhanVien_module nv, String cosoId) {
        if (nv == null) {
            return;
        }
        String chucVu = nv.getChucVu() == null ? "" : nv.getChucVu().trim().toLowerCase();
        boolean isPhucVu = chucVu.contains("phục vụ") || chucVu.contains("phuc vu");

        if (!isPhucVu) {
            nv.setCoSo(null);
            return;
        }

        if (cosoId == null || cosoId.isBlank()) {
            throw new RuntimeException("Nhân viên phục vụ cần chọn cơ sở");
        }

        CoSo_module coso = cosoRepo.findById(cosoId)
                .orElseThrow(() -> new RuntimeException("Cơ sở không tồn tại"));
        nv.setCoSo(coso);
    }

    @CacheEvict(value = { "allEmployees", "adminEmployeePage" }, allEntries = true)
    @Transactional
    public void xoaNhanVien(String id) {
        nhanvienRepo.deleteById(id);
    }

    private String tuDongSinhMa() {
        Pageable pageable = PageRequest.of(0, 1);
        List<String> list = nhanvienRepo.findTopNhanVienId(pageable);

        if (list.isEmpty()) {
            return "NV001";
        }

        String lastId = list.get(0);
        String digits = lastId == null ? "" : lastId.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return "NV001";
        }
        int so = Integer.parseInt(digits);
        return String.format("NV%03d", so + 1);
    }
}
