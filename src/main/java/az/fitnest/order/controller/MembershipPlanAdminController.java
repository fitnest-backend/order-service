package az.fitnest.order.controller;

import az.fitnest.order.service.impl.MembershipPlanAdminService;
import az.fitnest.order.dto.MembershipPlanWithOptionsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/gyms/{gymId}/plans")
@RequiredArgsConstructor
public class MembershipPlanAdminController {

    private final MembershipPlanAdminService membershipPlanAdminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createPlan(@PathVariable Long gymId, @RequestBody MembershipPlanWithOptionsRequest request) {
        membershipPlanAdminService.addPlanWithOptions(gymId, request);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePlan(@PathVariable Long gymId, @PathVariable Long planId, @RequestBody MembershipPlanWithOptionsRequest request) {
        membershipPlanAdminService.updatePlanWithOptions(gymId, planId, request);
        return ResponseEntity.noContent().build();
    }

}
