package az.fitnest.order.subscription.api;

import az.fitnest.order.subscription.api.dto.ActiveSubscriptionResponse;
import az.fitnest.order.subscription.adapter.service.UserSubscriptionService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/subscriptions")
@RequiredArgsConstructor
@Tag(name = "User Subscriptions", description = "Endpoints for managing the authenticated user's subscriptions")
public class UserSubscriptionController {

    private final UserSubscriptionService subscriptionService;

    @Operation(summary = "Get active subscription", description = "Returns the active subscription details for the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active subscription retrieved",
                    content = @Content(schema = @Schema(implementation = ActiveSubscriptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/active")
    public ResponseEntity<ActiveSubscriptionResponse> getActiveSubscription() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(subscriptionService.getActiveSubscription(userId));
    }
}
