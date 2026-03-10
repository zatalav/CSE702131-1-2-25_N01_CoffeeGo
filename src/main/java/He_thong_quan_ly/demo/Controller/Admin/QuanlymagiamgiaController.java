package He_thong_quan_ly.demo.Controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Module.Admin.MaGiamGia_module;
import He_thong_quan_ly.demo.Service.Admin.QuanlymagiamgiaService;

@Controller
@RequestMapping("/admin/magiamgia")
public class QuanlymagiamgiaController {

    @Autowired
    private QuanlymagiamgiaService service;

    @GetMapping
    public String page(
            @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Page<MaGiamGia_module> pageData = service.getAllPaged(page, 10);
        model.addAttribute("dsMaGiamGia", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("maGiamGia", new MaGiamGia_module());
        return "Admin/Quanlymagiamgia";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute MaGiamGia_module maGiamGia) {
        service.themMaGiamGia(maGiamGia);
        return "redirect:/admin/magiamgia";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute MaGiamGia_module maGiamGia,
            RedirectAttributes redirect) {
        try {
            service.update(maGiamGia);
            redirect.addFlashAttribute("successMessage", "Cập nhật mã giảm giá thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/magiamgia";
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute MaGiamGia_module maGiamGia,
            RedirectAttributes redirect) {
        try {
            service.delete(maGiamGia.getMagiamgia_id());
            redirect.addFlashAttribute("successMessage", "Xóa mã giảm giá thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/magiamgia";
    }
}
