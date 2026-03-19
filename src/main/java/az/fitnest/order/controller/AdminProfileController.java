package az.fitnest.order.controller;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.dto.ErrorResponse;
import az.fitnest.order.dto.ErrorWrapperResponse;
import az.fitnest.order.service.impl.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import az.fitnest.order.exception.ResourceNotFoundException;
import az.fitnest.order.exception.BadRequestException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.context.MessageSource;
import java.util.Locale;

@RestController
@RequestMapping("/admin/subscription")
@RequiredArgsConstructor
public class AdminProfileController {
    private static final Logger log = LoggerFactory.getLogger(AdminProfileController.class);
    private final UserSubscriptionService userSubscriptionService;
    private final MessageSource messageSource;

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserSubscription(@PathVariable Long userId, WebRequest request, Locale locale) {
        log.info("Admin requested subscription details for userId={}", userId);
        try {
            ActiveSubscriptionResponse response = userSubscriptionService.getActiveSubscription(userId);
            log.info("Fetched subscription details for userId={}", userId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            log.error("ResourceNotFoundException for userId={}: {}", userId, ex.getMessage(), ex);
            String message = messageSource.getMessage("error.no_active_subscription", null, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorWrapperResponse.of(
                            "error.no_active_subscription",
                            message,
                            HttpStatus.NOT_FOUND.value(),
                            request.getDescription(false)
                    ));
        } catch (BadRequestException ex) {
            log.error("BadRequestException for userId={}: {}", userId, ex.getMessage(), ex);
            String message = messageSource.getMessage("error.bad_request", null, locale);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorWrapperResponse.of(
                            "error.bad_request",
                            message,
                            HttpStatus.BAD_REQUEST.value(),
                            request.getDescription(false)
                    ));
        } catch (Exception ex) {
            log.error("Unexpected exception for userId={}: {}", userId, ex.getMessage(), ex);
            ex.printStackTrace();
            String message = messageSource.getMessage("error.unexpected", null, locale);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorWrapperResponse.of(
                            "error.unexpected",
                            message,
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            request.getDescription(false)
                    ));
        }
    }

    @PostMapping("/{userId}/freeze")
    public ResponseEntity<Object> freezeUserSubscription(@PathVariable Long userId, WebRequest request, Locale locale) {
        log.info("Admin requested freeze for userId={}", userId);
        try {
            userSubscriptionService.freezeSubscription(userId);
            log.info("Subscription frozen for userId={}", userId);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException ex) {
            log.error("ResourceNotFoundException during freeze for userId={}: {}", userId, ex.getMessage(), ex);
            String message = messageSource.getMessage("error.no_active_subscription", null, locale);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorWrapperResponse.of(
                            "error.no_active_subscription",
                            message,
                            HttpStatus.NOT_FOUND.value(),
                            request.getDescription(false)
                    ));
        } catch (BadRequestException ex) {
            log.error("BadRequestException during freeze for userId={}: {}", userId, ex.getMessage(), ex);
            String message = messageSource.getMessage("error.bad_request", null, locale);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorWrapperResponse.of(
                            "error.bad_request",
                            message,
                            HttpStatus.BAD_REQUEST.value(),
                            request.getDescription(false)
                    ));
        } catch (Exception ex) {
            log.error("Unexpected exception during freeze for userId={}: {}", userId, ex.getMessage(), ex);
            String message = messageSource.getMessage("error.unexpected", null, locale);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorWrapperResponse.of(
                            "error.unexpected",
                            message,
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            request.getDescription(false)
                    ));
        }
    }
}
