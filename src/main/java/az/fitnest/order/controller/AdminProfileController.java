package az.fitnest.order.controller;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.service.impl.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/subscription")
@RequiredArgsConstructor
public class AdminProfileController {
    private final UserSubscriptionService userSubscriptionService;

    @GetMapping("/{userId}")
    public ResponseEntity<ActiveSubscriptionResponse> getUserSubscription(@PathVariable Long userId) {
        ActiveSubscriptionResponse response = userSubscriptionService.getActiveSubscription(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/freeze")
    public ResponseEntity<Void> freezeUserSubscription(@PathVariable Long userId) {
        userSubscriptionService.freezeSubscription(userId);
        return ResponseEntity.ok().build();
    }
}
