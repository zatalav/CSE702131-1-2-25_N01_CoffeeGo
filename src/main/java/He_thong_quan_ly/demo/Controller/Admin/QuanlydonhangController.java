package He_thong_quan_ly.demo.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import He_thong_quan_ly.demo.Service.Admin.QuanlydonhangService;

@Controller
public class QuanlydonhangController {
    private final QuanlydonhangService donhangService;

    public QuanlydonhangController(QuanlydonhangService donhangService) {
        this.donhangService = donhangService;
    }

    @GetMapping("/admin/donhang")
    public String donhang(@RequestParam(value = "page", defaultValue = "0") int page,
            org.springframework.ui.Model model) {
        var pageData = donhangService.getOrderRowsPaged(page, 10);
        model.addAttribute("orders", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        java.util.List<String> types = java.util.Arrays.asList("Online", "Tại cửa hàng");
        model.addAttribute("types", types);

        return "Admin/Quanlydonhang";
    }
}