package He_thong_quan_ly.demo.Module.DTO;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreateRequest {
    private String note;
    private List<OrderItemRequest> items;
}

