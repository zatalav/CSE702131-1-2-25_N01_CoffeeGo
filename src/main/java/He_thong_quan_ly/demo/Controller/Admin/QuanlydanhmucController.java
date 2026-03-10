package He_thong_quan_ly.demo.Controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Module.Admin.DanhMuc_module;
import He_thong_quan_ly.demo.Service.Admin.QuanlydanhmucService;

@Controller
@RequestMapping("/admin/danhmuc")
public class QuanlydanhmucController {

    @Autowired
    private QuanlydanhmucService danhmucService;

    @GetMapping
    public String hienThi(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<DanhMuc_module> pageData = danhmucService.findAllPaged(page, 10);
        model.addAttribute("dsDanhMuc", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("danhMuc", new DanhMuc_module());
        return "Admin/Quanlydanhmuc";
    }

    @PostMapping("/add")
    public String them(
            @ModelAttribute("danhMuc") DanhMuc_module dm,
            RedirectAttributes redirectAttributes) {
        boolean thanhCong = danhmucService.themDanhMuc(dm);

        if (!thanhCong) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Tên danh mục đã tồn tại!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Thêm danh mục thành công!");
        }

        return "redirect:/admin/danhmuc";
    }

    @PostMapping("/update")
    public String update(@RequestParam String danhmucId,
            @RequestParam String tenDm,
            RedirectAttributes redirect) {
        try {
            danhmucService.update(danhmucId, tenDm);
            redirect.addFlashAttribute("successMessage", "Cập nhật danh mục thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/danhmuc";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam String danhmucId,
            RedirectAttributes redirect) {
        try {
            danhmucService.delete(danhmucId);
            redirect.addFlashAttribute("successMessage", "Xóa danh mục thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/danhmuc";
    }

}
