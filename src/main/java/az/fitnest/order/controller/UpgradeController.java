package az.fitnest.order.controller;

import az.fitnest.order.dto.UpgradeCheckoutRequest;
import az.fitnest.order.dto.UpgradeCheckoutResponse;
import az.fitnest.order.dto.UpgradeOptionsResponse;
import az.fitnest.order.model.entity.Order;
import az.fitnest.order.service.impl.UpgradeService;
import az.fitnest.order.dto.PaymentResultDto;
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
@Tag(name = "Subscription Upgrades", description = "Abunəliklərin yüksəldilməsi və sifarişlərin idarə olunması üçün ucluqlar")
public class UpgradeController {

    private final UpgradeService upgradeService;

    @Operation(
            summary = "Yüksəltmə seçimlərini əldə edin",
            description = "Autentifikasiya olunmuş istifadəçi üçün mövcud abunəlik yüksəltmə seçimlərini qaytarır. " +
                    "İstəyə bağlı olaraq hədəf müddətə görə süzgəcdən keçirə bilər."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Yüksəltmə seçimləri uğurla əldə edildi",
                    content = @Content(schema = @Schema(implementation = UpgradeOptionsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "İcazə verilmədi",
                    content = @Content
            )
    })
    @GetMapping("/subscriptions/upgrade/options")
    public ResponseEntity<UpgradeOptionsResponse> getUpgradeOptions(
            @Parameter(description = "Yüksəltmə seçimlərini süzgəcdən keçirmək üçün ay ilə hədəf müddət")
            @RequestParam(name = "target_duration_months", required = false) Integer targetDurationMonths) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(upgradeService.getUpgradeOptions(userId, targetDurationMonths));
    }

    @Operation(
            summary = "Abunəlik yüksəltmə ödənişi",
            description = "Abunəlik yüksəltmə ödəniş prosesini icra edir. " +
                    "Ödəniş uğursuz olarsa 402 Payment Required, uğurlu olarsa 200 OK qaytarır."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ödəniş uğurla emal edildi",
                    content = @Content(schema = @Schema(implementation = UpgradeCheckoutResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Yanlış sorğu məlumatı",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "İcazə verilmədi",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "402",
                    description = "Ödəniş tələb olunur - ödəniş uğursuz oldu",
                    content = @Content(schema = @Schema(implementation = UpgradeCheckoutResponse.class))
            )
    })
    @PostMapping("/subscriptions/upgrade/checkout")
    public ResponseEntity<UpgradeCheckoutResponse> checkout(
            @Parameter(description = "Yüksəltmə ödəniş təfərrüatları") @Valid @RequestBody UpgradeCheckoutRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UpgradeCheckoutResponse response = upgradeService.checkout(userId, request);
        
        if (response.getPayment() != null && !"success".equals(response.getPayment().getStatus())) {
            return ResponseEntity.status(402).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Sifariş təfərrüatlarını əldə edin",
            description = "Sifariş ID-si vasitəsilə xüsusi sifarişin təfərrüatlarını qaytarır."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sifariş təfərrüatları uğurla əldə edildi",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "İcazə verilmədi",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sifariş tapılmadı",
                    content = @Content
            )
    })
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(
            @Parameter(description = "Sifarişin ID-si") @PathVariable String orderId) {
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
