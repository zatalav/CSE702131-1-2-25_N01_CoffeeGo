package He_thong_quan_ly.demo.Module.bang_phu_id;

import java.io.Serializable;

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
public class XuatKho_CoSo_id implements Serializable {
    private String xuatkho_id;
    private String coso_id;
}
