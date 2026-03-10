package He_thong_quan_ly.demo.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Module.Admin.CoSo_module;
import He_thong_quan_ly.demo.Service.Admin.QuanlycosoService;

import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/admin/coso")
public class Quanlycoso_Controller {

    private final QuanlycosoService cosoService;

    public Quanlycoso_Controller(QuanlycosoService cosoService) {
        this.cosoService = cosoService;
    }

    @GetMapping
    public String hienThi(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<CoSo_module> pageData = cosoService.findAllPaged(page, 10);
        model.addAttribute("dsCoSo", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("coSo", new CoSo_module());
        return "Admin/Quanlycoso";
    }

    @PostMapping("/add")
    public String them(@ModelAttribute("coSo") CoSo_module coso, RedirectAttributes redirect) {
        try {
            cosoService.create(coso);
            redirect.addFlashAttribute("successMessage", "Thêm cơ sở thành công!");
        } catch (Exception ex) {
            redirect.addFlashAttribute("errorMessage", "Thêm cơ sở thất bại!");
        }
        return "redirect:/admin/coso";
    }

    @PostMapping("/update")
    public String capNhat(@ModelAttribute CoSo_module coso, RedirectAttributes redirect) {
        try {
            cosoService.update(coso);
            redirect.addFlashAttribute("successMessage", "Cập nhật cơ sở thành công!");
        } catch (Exception ex) {
            redirect.addFlashAttribute("errorMessage", "Cập nhật cơ sở thất bại!");
        }
        return "redirect:/admin/coso";
    }

    @PostMapping("/delete")
    public String xoa(@RequestParam("cosoId") String cosoId, RedirectAttributes redirect) {
        try {
            cosoService.delete(cosoId);
            redirect.addFlashAttribute("successMessage", "Xóa cơ sở thành công!");
        } catch (Exception ex) {
            redirect.addFlashAttribute("errorMessage", "Xóa cơ sở thất bại!");
        }
        return "redirect:/admin/coso";
    }
}
