package az.fitnest.order.controller;

import az.fitnest.order.service.impl.MembershipPlanAdminService;
import az.fitnest.order.dto.MembershipPlanWithOptionsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
public class MembershipPlanAdminController {

    private final MembershipPlanAdminService membershipPlanAdminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createPlan(@RequestBody MembershipPlanWithOptionsRequest request) {
        membershipPlanAdminService.addPlanWithOptions(request);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePlan(@PathVariable Long planId, @RequestBody MembershipPlanWithOptionsRequest request) {
        membershipPlanAdminService.updatePlanWithOptions(planId, request);
        return ResponseEntity.noContent().build();
    }

}
