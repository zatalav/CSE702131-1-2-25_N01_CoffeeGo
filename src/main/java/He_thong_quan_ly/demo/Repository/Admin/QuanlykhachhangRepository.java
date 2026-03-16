package He_thong_quan_ly.demo.Repository.Admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import He_thong_quan_ly.demo.Module.Admin.KhachHang_module;

public interface QuanlykhachhangRepository extends JpaRepository<KhachHang_module, String> {

        @Query("select count(k) > 0 from KhachHang_module k where lower(k.Gmail) = lower(:gmail)")
        boolean existsByGmailIgnoreCase(@Param("gmail") String gmail);

        @Query("select count(k) > 0 from KhachHang_module k where k.SDT = :sdt")
        boolean existsBySDT(@Param("sdt") String sdt);

        @Query("select k.khachhang_id from KhachHang_module k order by k.khachhang_id desc")
        List<String> findTopKhachHangId(Pageable pageable);

        @Query("""
                        select k
                        from KhachHang_module k
                                                            where (k.Gmail = :login or k.SDT = :login)
                          and k.Password = :password
                        """)
        Optional<KhachHang_module> findByLoginAndPassword(
                        @Param("login") String login,
                        @Param("password") String password);

        @Query("""
                        select k
                        from KhachHang_module k
                                    where k.Gmail = :login or k.SDT = :login
                        """)
        Optional<KhachHang_module> findByLogin(@Param("login") String login);

        @Query("""
                        SELECT COUNT(k)
                        FROM KhachHang_module k
                        WHERE k.Trang_thai IS NULL OR upper(k.Trang_thai) <> 'KHOA'
                        """)
        long countActiveCustomers();
}
