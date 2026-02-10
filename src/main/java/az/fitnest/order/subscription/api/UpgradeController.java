package az.fitnest.order.subscription.api;

import az.fitnest.order.subscription.api.dto.UpgradeCheckoutRequest;
import az.fitnest.order.subscription.api.dto.UpgradeCheckoutResponse;
import az.fitnest.order.subscription.api.dto.UpgradeOptionsResponse;
import az.fitnest.order.subscription.domain.model.Order;
import az.fitnest.order.subscription.adapter.service.UpgradeService;
import az.fitnest.order.subscription.api.dto.PaymentResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing subscription upgrades and orders.
 * Provides endpoints for viewing upgrade options, processing upgrades, and checking order status.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Subscription Upgrades", description = "Endpoints for upgrading subscriptions and managing orders")
public class UpgradeController {

    private final UpgradeService upgradeService;

    @Operation(
            summary = "Get upgrade options",
            description = "Returns available subscription upgrade options for the authenticated user. " +
                    "Can optionally filter by target duration."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Upgrade options retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UpgradeOptionsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            )
    })
    @GetMapping("/subscriptions/upgrade/options")
    public ResponseEntity<UpgradeOptionsResponse> getUpgradeOptions(
            @Parameter(description = "Target duration in months for filtering upgrade options")
            @RequestParam(name = "target_duration_months", required = false) Integer targetDurationMonths) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(upgradeService.getUpgradeOptions(userId, targetDurationMonths));
    }

    @Operation(
            summary = "Checkout subscription upgrade",
            description = "Processes the subscription upgrade checkout. " +
                    "Returns 402 Payment Required if payment fails, 200 OK on success."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Checkout processed successfully",
                    content = @Content(schema = @Schema(implementation = UpgradeCheckoutResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "402",
                    description = "Payment required - payment failed",
                    content = @Content(schema = @Schema(implementation = UpgradeCheckoutResponse.class))
            )
    })
    @PostMapping("/subscriptions/upgrade/checkout")
    public ResponseEntity<UpgradeCheckoutResponse> checkout(
            @Parameter(description = "Upgrade checkout details") @Valid @RequestBody UpgradeCheckoutRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UpgradeCheckoutResponse response = upgradeService.checkout(userId, request);
        
        if (response.getPayment() != null && !"success".equals(response.getPayment().getStatus())) {
            return ResponseEntity.status(402).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Get order details",
            description = "Returns the details of a specific order by order ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content
            )
    })
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(
            @Parameter(description = "ID of the order") @PathVariable String orderId) {
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
