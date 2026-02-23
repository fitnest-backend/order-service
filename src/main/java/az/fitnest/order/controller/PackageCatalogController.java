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
@Tag(name = "Subscription Packages", description = "Endpoints for browsing available subscription packages")
public class PackageCatalogController {

    private final PackageCatalogService packageCatalogService;

    @Operation(summary = "Get all packages", description = "Returns a list of all subscription packages.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PackageListResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PackageListResponse> getAllPackages(@RequestParam(defaultValue = "true") boolean active_only) {
        return ResponseEntity.ok(packageCatalogService.getAllPackages(active_only));
    }

    @Operation(summary = "Get package by ID", description = "Returns details of a specific subscription package.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Package found",
                    content = @Content(schema = @Schema(implementation = SubscriptionPackageDto.class))),
            @ApiResponse(responseCode = "404", description = "Package not found")
    })
    @GetMapping("/{packageId}")
    public ResponseEntity<SubscriptionPackageDto> getPackageById(@PathVariable Long packageId) {
        return ResponseEntity.ok(packageCatalogService.getPackageById(packageId));
    }
}
