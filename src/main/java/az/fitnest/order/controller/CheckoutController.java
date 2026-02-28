package az.fitnest.order.controller;

import az.fitnest.order.dto.CheckoutRequest;
import az.fitnest.order.dto.CheckoutResponse;
import az.fitnest.order.service.CheckoutService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Abunəliklər və idman paketləri almaq üçün ucluqlar")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @Operation(summary = "Ödəniş prosesini icra edin", description = "Xüsusi paket üçün ödəniş prosesini başladır. İstifadəçi autentifikasiyadan keçməlidir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ödəniş uğurla başladıldı",
                    content = @Content(schema = @Schema(implementation = CheckoutResponse.class))),
            @ApiResponse(responseCode = "400", description = "Doğrulama xətası"),
            @ApiResponse(responseCode = "401", description = "İcazə verilmədi - Satınalma üçün istifadəçi daxil olmalıdır"),
            @ApiResponse(responseCode = "404", description = "Paket tapılmadı")
    })
    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(
            @AuthenticationPrincipal Object principal,
            @Parameter(description = "Ödəniş üçün paket və ödəniş təfərrüatları") @Valid @RequestBody CheckoutRequest request) {
        Long userId = extractUserId(principal);
        if (userId == null) {
             throw new RuntimeException("User must be authenticated");
        }
        return ResponseEntity.status(201).body(checkoutService.processCheckout(userId, request));
    }

    private Long extractUserId(Object principal) {
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal != null) {
            try {
                return Long.parseLong(principal.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
