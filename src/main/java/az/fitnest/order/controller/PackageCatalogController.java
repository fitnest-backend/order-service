package az.fitnest.order.controller;

import az.fitnest.order.dto.PackageListResponse;
import az.fitnest.order.dto.PackagePlanListResponse;
import az.fitnest.order.dto.SubscriptionPackageDto;
import az.fitnest.order.dto.SubscriptionPackageResponse;
import az.fitnest.order.service.impl.PackageCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscription-packages")
@RequiredArgsConstructor
@Tag(name = "Subscription Packages", description = "Mövcud abunəlik paketlərinə baxmaq üçün ucluqlar")
public class PackageCatalogController {

    private final PackageCatalogService packageCatalogService;

    @Operation(summary = "Bütün paketləri əldə edin", description = "Bütün unikal abunəlik paketlərinin siyahısını qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paketlər uğurla əldə edildi",
                    content = @Content(schema = @Schema(implementation = PackagePlanListResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PackagePlanListResponse> getAllPackages() {
        return ResponseEntity.ok(packageCatalogService.getUniquePlans());
    }

    @Operation(summary = "Paket təfərrüatlarını əldə edin", description = "Xüsusi abunəlik paketinin təfərrüatlarını package ID ilə qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paket tapıldı",
                    content = @Content(schema = @Schema(implementation = SubscriptionPackageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paket tapılmadı")
    })
    @GetMapping("/{packageId}")
    public ResponseEntity<SubscriptionPackageResponse> getPlanById(@PathVariable Long packageId) {
        return ResponseEntity.ok(packageCatalogService.getPlanById(packageId));
    }

    @Operation(summary = "Paketin bütün variantlarını əldə edin", description = "Xüsusi abunəlik paketinin bütün variantlarını qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Variantlar əldə edildi")
    })
    @GetMapping("/{packageId}/options")
    public ResponseEntity<java.util.List<az.fitnest.order.dto.PackageOptionDto>> getOptionsByPlanId(@PathVariable Long packageId) {
        return ResponseEntity.ok(packageCatalogService.getOptionsByPlanId(packageId));
    }

    @Operation(summary = "Paket variantının təfərrüatlarını əldə edin", description = "Xüsusi abunəlik paketi variantının təfərrüatlarını package ID və option ID ilə qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Variant tapıldı",
                    content = @Content(schema = @Schema(implementation = SubscriptionPackageDto.class))),
            @ApiResponse(responseCode = "404", description = "Variant tapılmadı")
    })
    @GetMapping("/{packageId}/options/{optionId}")
    public ResponseEntity<SubscriptionPackageDto> getOptionDetails(@PathVariable Long packageId, @PathVariable Long optionId) {
        return ResponseEntity.ok(packageCatalogService.getOptionDetails(packageId, optionId));
    }
}
