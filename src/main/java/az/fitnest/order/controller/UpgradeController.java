package az.fitnest.order.controller;

import az.fitnest.order.dto.UpgradeCheckoutRequest;
import az.fitnest.order.dto.UpgradeCheckoutResponse;
import az.fitnest.order.dto.UpgradeOptionsResponse;
import az.fitnest.order.model.entity.Order;
import az.fitnest.order.service.impl.UpgradeService;
import az.fitnest.order.dto.PaymentResultDto;
import az.fitnest.order.dto.OrderResponse;
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

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Subscription Upgrades", description = "Endpoints for managing subscription upgrades, payment processing, and order details. All endpoints require authentication.")
public class UpgradeController {

    private final UpgradeService upgradeService;

    @Operation(
        summary = "Get available upgrade options",
        description = "Returns available upgrade options for the authenticated user's current subscription. Optionally filter by target duration in months.",
        parameters = {
            @Parameter(
                name = "target_duration_months",
                description = "Filter upgrade options by target duration in months.",
                example = "12",
                required = false
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Upgrade options successfully retrieved.",
            content = @Content(schema = @Schema(implementation = UpgradeOptionsResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication required.",
            content = @Content
        )
    })
    @GetMapping("/subscriptions/upgrade/options")
    public ResponseEntity<UpgradeOptionsResponse> getUpgradeOptions(
        @RequestParam(name = "target_duration_months", required = false)
        Integer targetDurationMonths) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(upgradeService.getUpgradeOptions(userId, targetDurationMonths));
    }

    @Operation(
        summary = "Process subscription upgrade payment",
        description = "Processes payment for a subscription upgrade. Returns payment and subscription details. If payment fails, returns 402 Payment Required.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Upgrade payment details",
            required = true,
            content = @Content(schema = @Schema(implementation = UpgradeCheckoutRequest.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment processed successfully.",
            content = @Content(schema = @Schema(implementation = UpgradeCheckoutResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request. Invalid input data.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication required.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "402",
            description = "Payment required. Payment failed.",
            content = @Content(schema = @Schema(implementation = UpgradeCheckoutResponse.class))
        )
    })
    @PostMapping("/subscriptions/upgrade/checkout")
    public ResponseEntity<UpgradeCheckoutResponse> checkout(
        @Valid @RequestBody UpgradeCheckoutRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UpgradeCheckoutResponse response = upgradeService.checkout(userId, request);
        if (response.payment() != null && !"success".equals(response.payment().status())) {
            return ResponseEntity.status(402).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get order details",
        description = "Returns detailed information for a specific order belonging to the authenticated user.",
        parameters = {
            @Parameter(
                name = "orderId",
                description = "Unique identifier of the order.",
                example = "ord_12345678",
                required = true
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order details successfully retrieved.",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication required.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found or does not belong to the user.",
            content = @Content
        )
    })
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
        @PathVariable String orderId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OrderResponse response = upgradeService.getOrderResponse(userId, orderId);
        return ResponseEntity.ok(response);
    }
}
