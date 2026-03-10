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

import He_thong_quan_ly.demo.Module.Admin.NhaCungCap_module;
import He_thong_quan_ly.demo.Service.Admin.QuanlynhacungcapService;

@Controller
@RequestMapping("/admin/nhacungcap")
public class QuanlynhacungcapController {

    @Autowired
    private QuanlynhacungcapService service;

    @GetMapping
    public String hienThi(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<NhaCungCap_module> pageData = service.getAllPaged(page, 10);
        model.addAttribute("dsNhaCungCap", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("nhaCungCap", new NhaCungCap_module());
        return "Admin/Quanlynhacungcap";
    }

    @PostMapping("/add")
    public String them(
            @ModelAttribute("nhaCungCap") NhaCungCap_module ncc,
            RedirectAttributes redirectAttributes) {
        boolean thanhCong = service.themNhaCungCap(ncc);

        if (!thanhCong) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Nhà cung cấp đã tồn tại!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thêm nhà cung cấp thành công!");
        }

        return "redirect:/admin/nhacungcap";
    }

    // ===== UPDATE =====
    @PostMapping("/update")
    public String update(
            @ModelAttribute NhaCungCap_module nhaCungCap,
            RedirectAttributes ra) {
        try {
            service.update(nhaCungCap);
            ra.addFlashAttribute("successMessage", "Cập nhật nhà cung cấp thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/nhacungcap";
    }

    // ===== DELETE =====
    @PostMapping("/delete")
    public String delete(
            @RequestParam String nhacungcapId,
            RedirectAttributes ra) {
        try {
            service.delete(nhacungcapId);
            ra.addFlashAttribute("successMessage", "Xóa nhà cung cấp thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/nhacungcap";
    }
}
