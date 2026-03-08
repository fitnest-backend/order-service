package az.fitnest.order.controller;

import az.fitnest.order.dto.PackageListResponse;
import az.fitnest.order.dto.SubscriptionPackageDto;
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

    @Operation(summary = "Bütün paketləri əldə edin", description = "Bütün abunəlik paketlərinin siyahısını qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paketlər uğurla əldə edildi",
                    content = @Content(schema = @Schema(implementation = PackageListResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PackageListResponse> getAllPackages(@RequestParam(defaultValue = "true") boolean active_only) {
        return ResponseEntity.ok(packageCatalogService.getAllPackages(active_only));
    }

    @Operation(summary = "Paketi option ID vasitəsilə əldə edin", description = "Xüsusi abunəlik paketinin təfərrüatlarını option ID ilə qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paket tapıldı",
                    content = @Content(schema = @Schema(implementation = SubscriptionPackageDto.class))),
            @ApiResponse(responseCode = "404", description = "Paket tapılmadı")
    })
    @GetMapping("/{optionId}")
    public ResponseEntity<SubscriptionPackageDto> getPackageByOptionId(@PathVariable Long optionId) {
        return ResponseEntity.ok(packageCatalogService.getPackageByOptionId(optionId));
    }

    @Operation(summary = "Bütün paket variantlarını əldə edin", description = "Bütün abunəlik paket variantlarının siyahısını qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Variantlar uğurla əldə edildi",
                    content = @Content(schema = @Schema(implementation = PackageListResponse.class)))
    })
    @GetMapping("/options")
    public ResponseEntity<PackageListResponse> getAllOptions(@RequestParam(defaultValue = "true") boolean active_only) {
        return ResponseEntity.ok(packageCatalogService.getAllPackages(active_only));
    }

    @Operation(summary = "Paket variantını option ID vasitəsilə əldə edin (təfərrüatlı)", description = "Xüsusi abunəlik paketi variantının təfərrüatlarını (faydalar daxil olmaqla) option ID ilə qaytarır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Variant tapıldı",
                    content = @Content(schema = @Schema(implementation = SubscriptionPackageDto.class))),
            @ApiResponse(responseCode = "404", description = "Variant tapılmadı")
    })
    @GetMapping("/options/{optionId}")
    public ResponseEntity<SubscriptionPackageDto> getOptionDetails(@PathVariable Long optionId) {
        return ResponseEntity.ok(packageCatalogService.getPackageByOptionId(optionId));
    }
}
