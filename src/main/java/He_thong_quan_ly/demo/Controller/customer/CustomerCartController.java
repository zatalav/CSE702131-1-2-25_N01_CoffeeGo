package He_thong_quan_ly.demo.Controller.customer;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import He_thong_quan_ly.demo.Module.DTO.CartAddRequest;
import He_thong_quan_ly.demo.Module.DTO.CartQuantityRequest;
import He_thong_quan_ly.demo.Module.DTO.CartRemoveRequest;
import He_thong_quan_ly.demo.Service.customer.cart.CustomerCartService;

@RestController
@RequestMapping("/customer/cart")
public class CustomerCartController {
    private final CustomerCartService customerCartService;

    public CustomerCartController(
            CustomerCartService customerCartService) {
        this.customerCartService = customerCartService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartAddRequest request) {
        return customerCartService.addToCart(request, resolveCurrentCustomerId());
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody CartRemoveRequest request) {
        return customerCartService.removeFromCart(request, resolveCurrentCustomerId());
    }

    @PostMapping("/update-qty")
    public ResponseEntity<?> updateQuantity(@RequestBody CartQuantityRequest request) {
        return customerCartService.updateQuantity(request, resolveCurrentCustomerId());
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam("customerId") String customerId) {
        String resolvedCustomerId = isBlank(customerId) ? resolveCurrentCustomerId() : customerId;
        return customerCartService.count(resolvedCustomerId);
    }

    @GetMapping("/items")
    public ResponseEntity<?> items(@RequestParam(value = "customerId", required = false) String customerId) {
        String resolvedCustomerId = isBlank(customerId) ? resolveCurrentCustomerId() : customerId;
        return customerCartService.items(resolvedCustomerId);
    }

    private String resolveCurrentCustomerId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        if (auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
