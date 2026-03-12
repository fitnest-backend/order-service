package az.fitnest.order.controller;

import az.fitnest.order.dto.AdminSubscriptionPackageResponse;
import az.fitnest.order.dto.ApiResponse;
import az.fitnest.order.service.impl.SubscriptionPackageAdminService;
import az.fitnest.order.dto.SubscriptionPackageWithOptionsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/subscription-packages")
@RequiredArgsConstructor
@Tag(name = "Subscription Package Admin", description = "Abunəlik paketlərini idarə etmək üçün administrativ ucluqlar")
public class SubscriptionPackageAdminController {

    private final SubscriptionPackageAdminService subscriptionPackageAdminService;

    @Operation(summary = "Bütün paketləri əldə edin", description = "Bütün abunəlik paketlərini variantları ilə birlikdə qaytarır.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminSubscriptionPackageResponse>>> getAllPackages() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPackageAdminService.getAllPackages()));
    }

    @Operation(summary = "Paketi ID ilə əldə edin", description = "Müəyyən abunəlik paketini variantları ilə birlikdə qaytarır.")
    @GetMapping("/{packageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminSubscriptionPackageResponse>> getPackageById(@PathVariable Long packageId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPackageAdminService.getPackageById(packageId)));
    }

    @Operation(summary = "Paket yaradın", description = "Yeni abunəlik paketi yaradır. ADMIN rolu tələb olunur.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createPackage(@RequestBody SubscriptionPackageWithOptionsRequest request) {
        subscriptionPackageAdminService.addPackageWithOptions(request);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Paketi yeniləyin", description = "Mövcud abunəlik paketini yeniləyir. ADMIN rolu tələb olunur.")
    @PutMapping("/{packageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePackage(@PathVariable Long packageId, @RequestBody SubscriptionPackageWithOptionsRequest request) {
        subscriptionPackageAdminService.updatePackageWithOptions(packageId, request);
        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
=======
    @Operation(summary = "Paket variantı yaradın", description = "Mövcud abunəlik paketinə yeni variant əlavə edir. ADMIN rolu tələb olunur.")
    @PostMapping("/{packageId}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createOption(@PathVariable Long packageId, @RequestBody az.fitnest.order.dto.PackageOptionEntityDto request) {
        Long optionId = subscriptionPackageAdminService.addOptionToPackage(packageId, request);
        return ResponseEntity.status(201).body(optionId);
    }

    @Operation(summary = "Sadə paket yaradın", description = "Yalnız əsas sahələrlə yeni abunəlik paketi yaradır. ADMIN rolu tələb olunur.")
    @PostMapping("/basic")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createPackage(@RequestBody az.fitnest.order.dto.SubscriptionPackageBasicRequest request) {
        Long packageId = subscriptionPackageAdminService.createPackage(request.name(), request.currency(), request.billingPeriod(), request.isActive());
        return ResponseEntity.status(201).body(packageId);
    }

>>>>>>> 3dda0f5 (fix)
}
