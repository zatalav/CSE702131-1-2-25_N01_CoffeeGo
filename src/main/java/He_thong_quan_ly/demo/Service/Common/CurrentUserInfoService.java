package He_thong_quan_ly.demo.Service.Common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;

@Service
public class CurrentUserInfoService {

    private final QuanlynhanvienRepository nhanvienRepository;

    public CurrentUserInfoService(QuanlynhanvienRepository nhanvienRepository) {
        this.nhanvienRepository = nhanvienRepository;
    }

    @Cacheable(value = "currentUserBrief", key = "#username", condition = "#username != null && !#username.isBlank()")
    public Map<String, String> getBrief(String username) {
        Map<String, String> brief = new HashMap<>();
        if (username == null || username.isBlank()) {
            return brief;
        }

        var nv = nhanvienRepository.findByGmail(username)
                .or(() -> nhanvienRepository.findById(username))
                .orElse(null);

        if (nv != null) {
            brief.put("name", nv.getTenNv());
            brief.put("role", nv.getChucVu());
        }
        return brief;
    }
}