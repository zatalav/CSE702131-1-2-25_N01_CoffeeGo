package He_thong_quan_ly.demo.auth.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import He_thong_quan_ly.demo.Repository.Admin.QuanlykhachhangRepository;
import He_thong_quan_ly.demo.Repository.Admin.QuanlynhanvienRepository;
import He_thong_quan_ly.demo.auth.security.Sha256PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/truycap/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/",
                                "/login",
                                "/register",
                                "/register/**",
                                "/forgot-password/**",
                                "/customer/**",
                                "/logout")
                        .permitAll()
                        .requestMatchers("/admin/nguyenlieu/**", "/admin/sanpham/**")
                        .hasAnyRole("KHO", "ADMIN")
                        .requestMatchers("/dashboard", "/admin/**", "/api/dashboard/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/nhanvien/**")
                        .hasAnyRole("NV", "ADMIN", "GH")
                        .requestMatchers("/kho/**")
                        .hasAnyRole("KHO", "ADMIN", "NV")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(roleBasedSuccessHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            QuanlynhanvienRepository nhanvienRepo,
            QuanlykhachhangRepository khachhangRepo) {
        return username -> {
            var userOpt = nhanvienRepo.findByGmail(username);
            if (userOpt.isEmpty()) {
                userOpt = nhanvienRepo.findById(username);
            }

            if (userOpt.isPresent()) {
                var nv = userOpt.get();
                String role = normalizeRole(nv.getChucVu(), nv.getGmail(), nv.getNhanvienId());
                String principal = (nv.getGmail() == null || nv.getGmail().isBlank())
                        ? nv.getNhanvienId()
                        : nv.getGmail();

                if (principal == null || principal.isBlank()) {
                    throw new RuntimeException("Tai khoan nhan vien thieu thong tin dang nhap");
                }

                return User.withUsername(principal)
                        .password(nv.getPassword())
                        .authorities(new SimpleGrantedAuthority(role))
                        .build();
            }

            var khOpt = khachhangRepo.findByLogin(username);
            if (khOpt.isPresent()) {
                var kh = khOpt.get();
                return User.withUsername(kh.getKhachhang_id())
                        .password(kh.getPassword())
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                        .build();
            }

            throw new RuntimeException("Tai khoan khong ton tai");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Sha256PasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            if (hasRole(authentication, "ROLE_ADMIN")) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else if (hasRole(authentication, "ROLE_KHO")) {
                response.sendRedirect(request.getContextPath() + "/kho/dashboard");
            } else if (hasRole(authentication, "ROLE_GH")) {
                response.sendRedirect(request.getContextPath() + "/nhanvien/giaohang");
            } else if (hasRole(authentication, "ROLE_CUSTOMER")) {
                response.sendRedirect(request.getContextPath() + "/customer/menu?kh=" + authentication.getName());
            } else {
                response.sendRedirect(request.getContextPath() + "/nhanvien/donhang");
            }
        };
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }

    private String normalizeRole(String chucVu, String gmail, String nhanvienId) {
        if (chucVu == null || chucVu.isBlank()) {
            String fallback = ((gmail == null ? "" : gmail) + " " + (nhanvienId == null ? "" : nhanvienId))
                    .toLowerCase(Locale.ROOT);
            if (fallback.contains("admin")) {
                return "ROLE_ADMIN";
            }
            if (fallback.contains("kho")) {
                return "ROLE_KHO";
            }
            if (fallback.contains("giao") || fallback.contains("ship") || fallback.contains("vanchuyen")) {
                return "ROLE_GH";
            }
            return "ROLE_NV";
        }

        String normalized = chucVu.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("admin")
                || normalized.contains("quan tri")
                || normalized.contains("quản trị")
                || normalized.contains("quan ly")
                || normalized.contains("quản lý")
                || normalized.contains("chu quan")
                || normalized.contains("chủ quán")) {
            return "ROLE_ADMIN";
        }
        if (normalized.contains("nhan vien kho")
                || normalized.contains("nhân viên kho")
                || normalized.contains("nv kho")
                || normalized.contains("kho")) {
            return "ROLE_KHO";
        }
        if (normalized.contains("van chuyen")
                || normalized.contains("vận chuyển")
                || normalized.contains("giao hang")
                || normalized.contains("giao hàng")) {
            return "ROLE_GH";
        }
        if (normalized.contains("nv")
                || normalized.contains("nhan vien")
                || normalized.contains("nhân viên")
                || normalized.contains("phuc vu")
                || normalized.contains("phục vụ")) {
            return "ROLE_NV";
        }
        return "ROLE_NV";
    }
}
