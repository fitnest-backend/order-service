package az.fitnest.order.subscription.api;

import az.fitnest.order.subscription.api.dto.UpgradeCheckoutRequest;
import az.fitnest.order.subscription.api.dto.UpgradeCheckoutResponse;
import az.fitnest.order.subscription.api.dto.UpgradeOptionsResponse;
import az.fitnest.order.subscription.domain.model.Order;
import az.fitnest.order.subscription.adapter.service.UpgradeService;
import az.fitnest.order.subscription.api.dto.PaymentResultDto; // Reuse DTO for order response if suitable or map to map
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UpgradeController {

    private final UpgradeService upgradeService;

    @GetMapping("/subscriptions/upgrade/options")
    public ResponseEntity<UpgradeOptionsResponse> getUpgradeOptions(@RequestParam(name = "target_duration_months", required = false) Integer targetDurationMonths) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(upgradeService.getUpgradeOptions(userId, targetDurationMonths));
    }

    @PostMapping("/subscriptions/upgrade/checkout")
    public ResponseEntity<UpgradeCheckoutResponse> checkout(@Valid @RequestBody UpgradeCheckoutRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UpgradeCheckoutResponse response = upgradeService.checkout(userId, request);
        
        if (response.getPayment() != null && !"success".equals(response.getPayment().getStatus())) {
            return ResponseEntity.status(402).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String orderId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = upgradeService.getOrder(userId, orderId);
        
        return ResponseEntity.ok(Map.of(
            "order_id", order.getOrderId(),
            "type", order.getType(),
            "status", order.getStatus(),
            "amount", order.getAmount(),
            "currency", order.getCurrency(),
            "created_at", order.getCreatedAt()
        ));
    }
}
