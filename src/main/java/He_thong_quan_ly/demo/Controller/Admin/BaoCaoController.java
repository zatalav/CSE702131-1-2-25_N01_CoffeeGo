package He_thong_quan_ly.demo.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BaoCaoController {

    @GetMapping("/admin/baocao")
    public String baocao() {
        return "Admin/Baocao";
    }
}