package He_thong_quan_ly.demo.Controller.customer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import He_thong_quan_ly.demo.Repository.Admin.SanPhamVariantRepository;
import He_thong_quan_ly.demo.Service.Admin.QuanlydanhmucService;
import He_thong_quan_ly.demo.Service.Admin.QuanlysanphamService;

@Controller
@RequestMapping("/customer")
public class CustomerMenuController {

    private final QuanlysanphamService sanPhamService;
    private final QuanlydanhmucService danhMucService;
    private final SanPhamVariantRepository variantRepository;

    public CustomerMenuController(
            QuanlysanphamService sanPhamService,
            QuanlydanhmucService danhMucService,
            SanPhamVariantRepository variantRepository) {
        this.sanPhamService = sanPhamService;
        this.danhMucService = danhMucService;
        this.variantRepository = variantRepository;
    }

    @GetMapping
    public String customerHome() {
        return "redirect:/customer/menu";
    }

    @GetMapping("/menu")
    public String menu(Model model,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "size", required = false, defaultValue = "24") int size) {
        String khachhangId = null;

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()))) {
            khachhangId = auth.getName();
        }
        var page = sanPhamService.findActiveProductsPaged(size);
        model.addAttribute("dsSanPham", page.getContent());
        var sanPhamIds = page.getContent().stream()
                .map(sp -> sp.getSanPhamId())
                .filter(id -> id != null && !id.isBlank())
                .toList();

        Map<String, List<Map<String, Object>>> variantsByProduct = sanPhamIds.isEmpty()
                ? Collections.<String, List<Map<String, Object>>>emptyMap()
                : variantRepository.findBySanPham_SanPhamIdIn(sanPhamIds).stream()
                        .collect(Collectors.groupingBy(
                                v -> v.getSanPham().getSanPhamId(),
                                LinkedHashMap::new,
                                Collectors.mapping(v -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    String normalizedSize = v.getSize() == null ? ""
                                            : v.getSize().trim().toUpperCase(Locale.ROOT);
                                    m.put("size", normalizedSize);
                                    m.put("price", Math.round(v.getPrice()));
                                    return m;
                                }, Collectors.toList())));

        Map<String, Long> defaultPriceByProduct = new LinkedHashMap<>();

        for (var sp : page.getContent()) {

            List<Map<String, Object>> variants = variantsByProduct.getOrDefault(sp.getSanPhamId(),
                    Collections.emptyList());

            if (!variants.isEmpty()) {

                long price = ((Number) variants.get(0).get("price")).longValue();

                defaultPriceByProduct.put(sp.getSanPhamId(), price);

            } else {

                Long basePrice = sp.getGia();

                defaultPriceByProduct.put(
                        sp.getSanPhamId(),
                        basePrice != null ? basePrice : 0L);
            }
        }

        model.addAttribute("variantsByProduct", variantsByProduct);
        model.addAttribute("defaultPriceByProduct", defaultPriceByProduct);
        model.addAttribute("dsDanhMuc", danhMucService.findAll());
        model.addAttribute("registered", registered);
        model.addAttribute("customerId", khachhangId);
        model.addAttribute("nextSize", Math.min(size + 24, 60));
        model.addAttribute("hasMore", page.hasNext());
        return "Customer/Menu";
    }
}
