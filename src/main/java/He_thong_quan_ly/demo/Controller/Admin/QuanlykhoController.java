package He_thong_quan_ly.demo.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import He_thong_quan_ly.demo.Service.Admin.QuanlykhoService;

@Controller
public class QuanlykhoController {

    private final QuanlykhoService quanlykhoService;

    public QuanlykhoController(QuanlykhoService quanlykhoService) {
        this.quanlykhoService = quanlykhoService;
    }

    @GetMapping("/admin/kho")
    public String kho(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        var pageData = quanlykhoService.getKhoRowsPaged(page, 10);
        model.addAttribute("warehouses", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        return "Admin/Quanlykho";
    }
}