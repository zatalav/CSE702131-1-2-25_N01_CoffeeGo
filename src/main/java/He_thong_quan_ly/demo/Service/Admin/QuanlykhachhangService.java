package He_thong_quan_ly.demo.Service.Admin;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlykhachhangRepository;

@Service
@Transactional(readOnly = true)
public class QuanlykhachhangService {

    private final QuanlykhachhangRepository khachhangRepository;

    public QuanlykhachhangService(QuanlykhachhangRepository khachhangRepository) {
        this.khachhangRepository = khachhangRepository;
    }

    @Cacheable(value = "adminCustomerPage", key = "#page + '-' + #size")
    public Page<KhachHang_module> getAllPaged(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        return khachhangRepository.findAll(PageRequest.of(pageIndex, pageSize));
    }
}
