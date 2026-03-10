package He_thong_quan_ly.demo.Controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import He_thong_quan_ly.demo.Config.CloudinaryViewConfigResolver;
import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;
import He_thong_quan_ly.demo.Service.Admin.QuanlynhanvienService;

@Controller
@RequestMapping("/admin/nhanvien")
public class QuanlynhanvienController {

    @Autowired
    private QuanlynhanvienService nhanvienService;

    @Autowired
    private CloudinaryViewConfigResolver cloudinaryViewConfigResolver;

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.upload-preset:coffee_upload}")
    private String cloudinaryUploadPreset;

    @GetMapping
    public String hienThi(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<NhanVien_module> pageData = nhanvienService.getAllPaged(page, 10);
        model.addAttribute("dsNhanVien", pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("nhanVien", new NhanVien_module());
        model.addAttribute("dsCoSo", nhanvienService.getAllCoSo());
        model.addAttribute("cloudinaryCloudName", cloudinaryViewConfigResolver.resolveCloudName(cloudinaryCloudName));
        model.addAttribute("cloudinaryUploadPreset", cloudinaryUploadPreset);
        return "Admin/Quanlynhanvien";
    }

    @PostMapping("/add")
    public String themNhanVien(
            @ModelAttribute("nhanVien") NhanVien_module nv,
            @RequestParam(value = "cosoId", required = false) String cosoId,
            RedirectAttributes redirectAttributes) {

        try {
            nhanvienService.ganCoSo(nv, cosoId);
            nhanvienService.themNhanVien(nv);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên thành công");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/nhanvien";
    }

    @PostMapping("/update")
    public String capNhatNhanVien(
            @ModelAttribute("nhanVien") NhanVien_module nv,
            @RequestParam(value = "cosoId", required = false) String cosoId,
            RedirectAttributes redirectAttributes) {

        try {
            nhanvienService.ganCoSo(nv, cosoId);
            nhanvienService.capNhatNhanVien(nv);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/nhanvien";
    }

    @PostMapping("/delete")
    public String xoaNhanVien(@RequestParam("nhanvienId") String nhanvienId) {
        nhanvienService.xoaNhanVien(nhanvienId);
        return "redirect:/admin/nhanvien";
    }

}
