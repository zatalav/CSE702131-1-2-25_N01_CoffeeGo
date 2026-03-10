package He_thong_quan_ly.demo.Service.Kho;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import He_thong_quan_ly.demo.Module.bang_phu.CungCap;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynguyenlieuRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhacungcapRepository;
import He_thong_quan_ly.demo.Repository.bang_phu.CungCapRepository;

@Service
@Transactional(readOnly = true)
public class KhoMasterDataService {

    private static final Logger log = LoggerFactory.getLogger(KhoMasterDataService.class);

    private final QuanlynguyenlieuRepository nguyenlieuRepository;
    private final QuanlynhacungcapRepository nhacungcapRepository;
    private final CungCapRepository cungCapRepository;

    public KhoMasterDataService(
            QuanlynguyenlieuRepository nguyenlieuRepository,
            QuanlynhacungcapRepository nhacungcapRepository,
            CungCapRepository cungCapRepository) {
        this.nguyenlieuRepository = nguyenlieuRepository;
        this.nhacungcapRepository = nhacungcapRepository;
        this.cungCapRepository = cungCapRepository;
    }

    @Cacheable("khoMasterData")
    public Map<String, Object> getMasterData() {
        var dsNguyenLieu = nguyenlieuRepository.findAll();
        var dsNhaCungCap = nhacungcapRepository.findAll();
        List<CungCap> cungCapList;
        try {
            cungCapList = cungCapRepository.findAllWithRefs();
        } catch (Exception ex) {
            log.warn("Failed to load CungCap mapping for kho master data, fallback to empty mapping", ex);
            cungCapList = List.of();
        }

        var validCungCap = cungCapList.stream()
                .filter(cc -> cc.getNguyenLieu() != null && cc.getNhaCungCap() != null)
                .toList();

        var dsNguyenLieuJs = dsNguyenLieu.stream().map(nl -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", nl.getNguyenlieuId());
            m.put("ten", nl.getTenNguyenLieu());
            m.put("donVi", nl.getDonVi());
            m.put("giaNhap", nl.getGiaNhap());
            return m;
        }).toList();

        var dsNhaCungCapJs = dsNhaCungCap.stream().map(ncc -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", ncc.getNhacungcapId());
            m.put("ten", ncc.getTenNhaCungCap());
            return m;
        }).toList();

        Map<String, List<String>> mapNguyenLieuNcc = validCungCap.stream()
                .collect(Collectors.groupingBy(
                        cc -> cc.getNguyenLieu().getNguyenlieuId(),
                        Collectors.mapping(cc -> cc.getNhaCungCap().getNhacungcapId(), Collectors.toList())));

        Map<String, List<String>> mapTenNguyenLieuNcc = validCungCap.stream()
                .collect(Collectors.groupingBy(
                        cc -> cc.getNguyenLieu().getTenNguyenLieu().toLowerCase(),
                        Collectors.mapping(cc -> cc.getNhaCungCap().getNhacungcapId(), Collectors.toList())));

        Map<String, List<String>> mapNccTenNguyenLieu = validCungCap.stream()
                .collect(Collectors.groupingBy(
                        cc -> cc.getNhaCungCap().getNhacungcapId(),
                        Collectors.mapping(cc -> cc.getNguyenLieu().getTenNguyenLieu(), Collectors.toList())));

        Map<String, Object> response = new HashMap<>();
        response.put("dsNguyenLieuJs", dsNguyenLieuJs);
        response.put("dsNhaCungCapJs", dsNhaCungCapJs);
        response.put("mapNguyenLieuNcc", mapNguyenLieuNcc);
        response.put("mapTenNguyenLieuNcc", mapTenNguyenLieuNcc);
        response.put("mapNccTenNguyenLieu", mapNccTenNguyenLieu);
        return response;
    }
}
