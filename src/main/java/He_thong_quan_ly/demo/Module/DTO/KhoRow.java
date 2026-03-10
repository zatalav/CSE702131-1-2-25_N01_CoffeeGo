package He_thong_quan_ly.demo.Module.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class KhoRow {
    private String maPhieu;
    private String loaiPhieu;
    private String tenNhanVien;
    private String tenNguyenLieu;
    private Integer soLuong;
    private String donVi;
    private String ghiChu;
}

