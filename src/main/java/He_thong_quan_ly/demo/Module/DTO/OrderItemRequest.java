package He_thong_quan_ly.demo.Module.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemRequest {
    private String id;
    private String name;
    private long price;
    private int qty;
    private String size;
    private String sugar;
    private String ice;
    private String milk;
    private String note;
}

