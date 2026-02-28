package az.fitnest.order.controller;

import az.fitnest.order.service.impl.MembershipPlanAdminService;
import az.fitnest.order.dto.MembershipPlanWithOptionsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
@Tag(name = "Membership Plan Admin", description = "Üzvlük planlarını və paketlərini idarə etmək üçün administrativ ucluqlar")
public class MembershipPlanAdminController {

    private final MembershipPlanAdminService membershipPlanAdminService;

    @Operation(summary = "Paket yaradın", description = "Yeni abunəlik paketi yaradır. ADMIN rolu tələb olunur.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createPlan(@RequestBody MembershipPlanWithOptionsRequest request) {
        membershipPlanAdminService.addPlanWithOptions(request);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Paketi yeniləyin", description = "Mövcud abunəlik paketini yeniləyir. ADMIN rolu tələb olunur.")
    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePlan(@PathVariable Long planId, @RequestBody MembershipPlanWithOptionsRequest request) {
        membershipPlanAdminService.updatePlanWithOptions(planId, request);
        return ResponseEntity.noContent().build();
    }

}
