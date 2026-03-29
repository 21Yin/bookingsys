package com.testing.bookingsys.controller;

import com.testing.bookingsys.dto.packageinfo.PackageCatalogResponse;
import com.testing.bookingsys.dto.packageinfo.PurchasePackageRequest;
import com.testing.bookingsys.dto.packageinfo.PurchasedPackageResponse;
import com.testing.bookingsys.service.PackageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PackageController {

    private final PackageService packageService;

    @GetMapping("/catalog/{countryCode}")
    public ResponseEntity<List<PackageCatalogResponse>> packagesByCountry(@PathVariable String countryCode) {
        return ResponseEntity.ok(packageService.getAvailablePackages(countryCode));
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchasedPackageResponse> purchase(@Valid @RequestBody PurchasePackageRequest request) {
        return ResponseEntity.ok(packageService.purchasePackage(request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PurchasedPackageResponse>> myPackages() {
        return ResponseEntity.ok(packageService.getMyPackages());
    }
}
