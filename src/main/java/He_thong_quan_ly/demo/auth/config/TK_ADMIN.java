package He_thong_quan_ly.demo.auth.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import He_thong_quan_ly.demo.Module.Admin.NhanVien_module;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;

@Configuration
public class TK_ADMIN {

    @Bean
    @SuppressWarnings("unused")
    CommandLineRunner seedDefaultAdmin(
            QuanlynhanvienRepository nhanvienRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "Admin@gmail.com";
            boolean exists = nhanvienRepository.findByGmail(adminEmail).isPresent();
            if (exists) {
                return;
            }
            NhanVien_module admin = new NhanVien_module();
            admin.setNhanvienId("NV_ADMIN");
            admin.setTenNv("Admin");
            admin.setChucVu("Admin");
            admin.setGmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("Admin123@"));
            admin.setNgaySinh(LocalDate.of(2000, 1, 1));
            admin.setSdt("0000000000");
            admin.setCccd("000000000000");
            admin.setDiaChi("Hệ thống");
            admin.setGioiTinh("Khác");
            nhanvienRepository.save(admin);
        };
    }
}

