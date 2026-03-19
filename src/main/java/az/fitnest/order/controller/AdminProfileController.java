package az.fitnest.order.controller;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.dto.ErrorResponse;
import az.fitnest.order.service.impl.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import az.fitnest.order.exception.ResourceNotFoundException;
import az.fitnest.order.exception.BadRequestException;

@RestController
@RequestMapping("/admin/subscription")
@RequiredArgsConstructor
public class AdminProfileController {
    private static final Logger log = LoggerFactory.getLogger(AdminProfileController.class);
    private final UserSubscriptionService userSubscriptionService;

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserSubscription(@PathVariable Long userId) {
        log.info("Admin requested subscription details for userId={}", userId);
        try {
            ActiveSubscriptionResponse response = userSubscriptionService.getActiveSubscription(userId);
            log.info("Fetched subscription details for userId={}", userId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            log.error("ResourceNotFoundException for userId={}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(ex.getMessage(), "error.no_active_subscription"));
        } catch (BadRequestException ex) {
            log.error("BadRequestException for userId={}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(ex.getMessage(), "error.bad_request"));
        } catch (Exception ex) {
            log.error("Unexpected exception for userId={}: {}", userId, ex.getMessage(), ex);
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("Unexpected error occurred", "error.unexpected"));
        }
    }

    @PostMapping("/{userId}/freeze")
    public ResponseEntity<Object> freezeUserSubscription(@PathVariable Long userId) {
        log.info("Admin requested freeze for userId={}", userId);
        try {
            userSubscriptionService.freezeSubscription(userId);
            log.info("Subscription frozen for userId={}", userId);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException ex) {
            log.error("ResourceNotFoundException during freeze for userId={}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(ex.getMessage(), "error.no_active_subscription"));
        } catch (BadRequestException ex) {
            log.error("BadRequestException during freeze for userId={}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(ex.getMessage(), "error.bad_request"));
        } catch (Exception ex) {
            log.error("Unexpected exception during freeze for userId={}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("Unexpected error occurred", "error.unexpected"));
        }
    }
}
