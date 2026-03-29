package com.testing.bookingsys.service;

import com.testing.bookingsys.dto.packageinfo.AdminPackageCatalogRequest;
import com.testing.bookingsys.dto.schedule.AdminCountryRequest;
import com.testing.bookingsys.dto.schedule.AdminScheduleRequest;
import com.testing.bookingsys.entity.ClassSchedule;
import com.testing.bookingsys.entity.Country;
import com.testing.bookingsys.entity.PackageCatalog;
import com.testing.bookingsys.exception.ApiException;
import com.testing.bookingsys.repository.ClassScheduleRepository;
import com.testing.bookingsys.repository.CountryRepository;
import com.testing.bookingsys.repository.PackageCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CountryRepository countryRepository;
    private final PackageCatalogRepository packageCatalogRepository;
    private final ClassScheduleRepository classScheduleRepository;

    @Transactional
    public Country createCountry(AdminCountryRequest request) {
        countryRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(country -> {
            throw new ApiException("Country code already exists");
        });
        return countryRepository.save(Country.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName())
                .build());
    }

    @Transactional
    public PackageCatalog createPackage(AdminPackageCatalogRequest request) {
        Country country = countryRepository.findByCodeIgnoreCase(request.getCountryCode())
                .orElseThrow(() -> new ApiException("Country not found"));
        return packageCatalogRepository.save(PackageCatalog.builder()
                .name(request.getName())
                .country(country)
                .credits(request.getCredits())
                .price(request.getPrice())
                .validityDays(request.getValidityDays())
                .active(true)
                .build());
    }

    @Transactional
    public ClassSchedule createSchedule(AdminScheduleRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ApiException("Schedule endTime must be after startTime");
        }
        Country country = countryRepository.findByCodeIgnoreCase(request.getCountryCode())
                .orElseThrow(() -> new ApiException("Country not found"));
        return classScheduleRepository.save(ClassSchedule.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .country(country)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .requiredCredits(request.getRequiredCredits())
                .capacity(request.getCapacity())
                .build());
    }
}
