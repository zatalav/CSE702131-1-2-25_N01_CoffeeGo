package He_thong_quan_ly.demo.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import He_thong_quan_ly.demo.Service.Admin.QuanlykhachhangService;

@Controller
public class QuanlykhachhangController {

    private final QuanlykhachhangService khachhangService;

    public QuanlykhachhangController(QuanlykhachhangService khachhangService) {
        this.khachhangService = khachhangService;
    }

    @GetMapping("/admin/khachhang")
    public String khachhang(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        var pageData = khachhangService.getAllPaged(page, 10);
        model.addAttribute("dsKhachHang", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        return "Admin/Quanlykhachhang";
    }
}