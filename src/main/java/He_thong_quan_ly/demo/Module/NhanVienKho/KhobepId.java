package He_thong_quan_ly.demo.Module.NhanVienKho;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class KhobepId implements Serializable {
    private String coSo;
    private String nguyenlieuId;
}
