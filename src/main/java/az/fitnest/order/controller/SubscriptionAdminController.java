package az.fitnest.order.controller;

import az.fitnest.order.dto.AdminAssignSubscriptionRequest;
import az.fitnest.order.dto.AdminAssignSubscriptionResponse;
import az.fitnest.order.dto.ApiResponse;
import az.fitnest.order.service.impl.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription Admin", description = "İstifadəçi abunəliklərini idarə etmək üçün administrativ ucluqlar")
public class SubscriptionAdminController {

    private final UserSubscriptionService userSubscriptionService;

    @Operation(
            summary = "İstifadəçiyə abunəlik planı təyin edin",
            description = "Admin tərəfindən istifadəçiyə üzvlük planı təyin edir. " +
                    "Əgər istifadəçinin mövcud aktiv və ya dondurulmuş abunəliyi varsa, əvvəlki ləğv olunur."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Abunəlik uğurla təyin edildi",
                    content = @Content(schema = @Schema(implementation = AdminAssignSubscriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Yanlış sorğu məlumatı"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan və ya müddət konfiqurasiyası tapılmadı")
    })
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminAssignSubscriptionResponse>> assignSubscription(
            @Valid @RequestBody AdminAssignSubscriptionRequest request) {
        AdminAssignSubscriptionResponse response = userSubscriptionService.assignSubscriptionToUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(
            summary = "İstifadəçinin abunəliyini ləğv edin",
            description = "Admin tərəfindən istifadəçinin aktiv və ya dondurulmuş abunəliyini ləğv edir."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Abunəlik uğurla ləğv edildi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Aktiv abunəlik tapılmadı")
    })
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeSubscription(@PathVariable Long userId) {
        userSubscriptionService.revokeSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
