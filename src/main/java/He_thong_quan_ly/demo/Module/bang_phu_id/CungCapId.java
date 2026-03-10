package He_thong_quan_ly.demo.Module.bang_phu_id;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CungCapId implements Serializable {

    @Column(name = "nguyenlieu_id")
    private String nguyenLieuId;

    @Column(name = "nhacungcap_id")
    private String nhaCungCapId;
}
