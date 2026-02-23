package az.fitnest.order.controller;

import az.fitnest.order.dto.CheckInRequest;
import az.fitnest.order.dto.CheckInResponse;
import az.fitnest.order.service.impl.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Check-In", description = "Gym QR code check-in endpoints")
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/checkin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Check in at a gym",
            description = "Scans the gym QR code and records a check-in. " +
                    "Validates the user's active membership for the gym and decrements the visit count."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check-in result",
                    content = @Content(schema = @Schema(implementation = CheckInResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CheckInResponse> checkIn(@RequestBody CheckInRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CheckInResponse response = checkInService.checkIn(userId, request.getGymId());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(403).body(response);
        }
    }
}
