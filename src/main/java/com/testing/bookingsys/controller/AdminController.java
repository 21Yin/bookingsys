package com.testing.bookingsys.controller;

import com.testing.bookingsys.dto.packageinfo.AdminPackageCatalogRequest;
import com.testing.bookingsys.dto.schedule.AdminCountryRequest;
import com.testing.bookingsys.dto.schedule.AdminScheduleRequest;
import com.testing.bookingsys.entity.ClassSchedule;
import com.testing.bookingsys.entity.Country;
import com.testing.bookingsys.entity.PackageCatalog;
import com.testing.bookingsys.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/countries")
    public ResponseEntity<Country> createCountry(@Valid @RequestBody AdminCountryRequest request) {
        return ResponseEntity.ok(adminService.createCountry(request));
    }

    @PostMapping("/packages")
    public ResponseEntity<PackageCatalog> createPackage(@Valid @RequestBody AdminPackageCatalogRequest request) {
        return ResponseEntity.ok(adminService.createPackage(request));
    }

    @PostMapping("/schedules")
    public ResponseEntity<ClassSchedule> createSchedule(@Valid @RequestBody AdminScheduleRequest request) {
        return ResponseEntity.ok(adminService.createSchedule(request));
    }
}
