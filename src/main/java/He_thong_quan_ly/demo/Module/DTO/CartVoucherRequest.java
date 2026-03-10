package He_thong_quan_ly.demo.Module.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartVoucherRequest {
    private String customerId;
    private String code;
}

