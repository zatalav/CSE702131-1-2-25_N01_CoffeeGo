package He_thong_quan_ly.demo.Module.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckoutRequest {
    private String customerId;
    private String address;
    private String paymentMethod;
    private String voucherCode;
    private String customerNote;
}

