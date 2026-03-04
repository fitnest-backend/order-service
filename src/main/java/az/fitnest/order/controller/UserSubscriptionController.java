package az.fitnest.order.controller;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.service.impl.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/subscriptions")
@RequiredArgsConstructor
@Tag(name = "User Subscriptions", description = "Autentifikasiya olunmuş istifadəçinin abunəliklərini idarə etmək üçün ucluqlar")
public class UserSubscriptionController {

    private final UserSubscriptionService subscriptionService;

    @Operation(summary = "Aktiv abunəliyi əldə edin", description = "Cari istifadəçi üçün aktiv abunəlik təfərrüatlarını qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aktiv abunəlik əldə edildi",
                    content = @Content(schema = @Schema(implementation = ActiveSubscriptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "İcazə verilmədi")
    })
    @GetMapping("/active")
    public ResponseEntity<ActiveSubscriptionResponse> getActiveSubscription() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(subscriptionService.getActiveSubscription(userId));
    }

    @Operation(summary = "Abunəliyi dondur", description = "Cari istifadəçinin aktiv abunəliyini dondurur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abunəlik donduruldu"),
            @ApiResponse(responseCode = "400", description = "Aktiv abunəlik yoxdur və ya artıq dondurulub"),
            @ApiResponse(responseCode = "401", description = "İcazə verilmədi")
    })
    @PostMapping("/freeze")
    public ResponseEntity<Void> freezeSubscription() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        subscriptionService.freezeSubscription(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Abunəliyi bərpa et", description = "Cari istifadəçinin dondurulmuş abunəliyini bərpa edir və qalan günləri hesablayır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abunəlik bərpa edildi"),
            @ApiResponse(responseCode = "400", description = "Dondurulmuş abunəlik yoxdur"),
            @ApiResponse(responseCode = "401", description = "İcazə verilmədi")
    })
    @PostMapping("/unfreeze")
    public ResponseEntity<Void> unfreezeSubscription() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        subscriptionService.unfreezeSubscription(userId);
        return ResponseEntity.ok().build();
    }
}
