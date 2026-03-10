package He_thong_quan_ly.demo.Controller.Admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Module.Admin.NguyenLieu_module;
import He_thong_quan_ly.demo.Service.Admin.QuanlynguyenlieuService;

@Controller
@RequestMapping("/admin/nguyenlieu")
public class QuanlynguyenlieuController {

    @Autowired
    private QuanlynguyenlieuService service;

    /* ================= HIỂN THỊ TRANG ================= */
    @GetMapping
    public String hienThiTrang(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {

        Page<NguyenLieu_module> pageData = service.getAllNguyenLieuPaged(page, 10);

        model.addAttribute("nguyenLieu", new NguyenLieu_module());
        model.addAttribute("dsNguyenLieu", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("dsNhaCungCap", service.getAllNhaCungCap());
        model.addAttribute("mode", "add");

        return "Admin/Quanlynguyenlieu";
    }

    /* ================= THÊM ================= */
    @PostMapping("/add")
    public String themNguyenLieu(
            @ModelAttribute NguyenLieu_module nguyenLieu,
            @RequestParam("nhaCungCapId") List<String> nhaCungCapIds,
            RedirectAttributes redirectAttributes) {

        try {
            service.themNguyenLieu(nguyenLieu, nhaCungCapIds);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thêm nguyên liệu thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/admin/nguyenlieu";
    }

    /* ================= XEM ================= */
    @GetMapping("/view/{id}")
    public String viewNguyenLieu(@PathVariable String id, Model model) {

        NguyenLieu_module nl = service.findById(id);

        model.addAttribute("nguyenLieu", nl);
        model.addAttribute("dsNhaCungCap", service.getAllNhaCungCap());
        model.addAttribute("mode", "view");

        return "Admin/Quanlynguyenlieu";
    }

    /* ================= SỬA (LOAD FORM) ================= */
    @GetMapping("/edit/{id}")
    public String editNguyenLieu(@PathVariable String id, Model model) {

        NguyenLieu_module nl = service.findById(id);

        model.addAttribute("nguyenLieu", nl);
        model.addAttribute("dsNhaCungCap", service.getAllNhaCungCap());
        model.addAttribute("mode", "edit");

        return "Admin/Quanlynguyenlieu";
    }

    /* ================= SỬA (SUBMIT) ================= */
    @PostMapping("/update")
    public String updateNguyenLieu(
            @ModelAttribute NguyenLieu_module nguyenLieu,
            @RequestParam("nhaCungCapId") List<String> nhaCungCapIds,
            RedirectAttributes redirectAttributes) {

        try {
            service.updateNguyenLieu(nguyenLieu, nhaCungCapIds);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật nguyên liệu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Cập nhật thất bại");
        }

        return "redirect:/admin/nguyenlieu";
    }

    /* ================= XÓA ================= */
    @GetMapping("/delete/{id}")
    public String deleteNguyenLieu(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {

        try {
            service.deleteById(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa nguyên liệu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa nguyên liệu đang được sử dụng");
        }

        return "redirect:/admin/nguyenlieu";
    }
}
