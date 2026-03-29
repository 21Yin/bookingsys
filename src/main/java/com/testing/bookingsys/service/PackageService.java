package com.testing.bookingsys.service;

import com.testing.bookingsys.dto.packageinfo.PackageCatalogResponse;
import com.testing.bookingsys.dto.packageinfo.PurchasePackageRequest;
import com.testing.bookingsys.dto.packageinfo.PurchasedPackageResponse;
import com.testing.bookingsys.entity.PackageCatalog;
import com.testing.bookingsys.entity.PurchasedPackage;
import com.testing.bookingsys.enums.CreditTransactionType;
import com.testing.bookingsys.enums.PackageStatus;
import com.testing.bookingsys.exception.ApiException;
import com.testing.bookingsys.integration.PaymentGateway;
import com.testing.bookingsys.repository.PackageCatalogRepository;
import com.testing.bookingsys.repository.PurchasedPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageService {

    private final PackageCatalogRepository packageCatalogRepository;
    private final PurchasedPackageRepository purchasedPackageRepository;
    private final UserService userService;
    private final PaymentGateway paymentGateway;
    private final CreditLedgerService creditLedgerService;

    public List<PackageCatalogResponse> getAvailablePackages(String countryCode) {
        return packageCatalogRepository.findByCountryCodeIgnoreCaseAndActiveTrue(countryCode).stream()
                .map(pkg -> PackageCatalogResponse.builder()
                        .id(pkg.getId())
                        .name(pkg.getName())
                        .countryCode(pkg.getCountry().getCode())
                        .countryName(pkg.getCountry().getName())
                        .credits(pkg.getCredits())
                        .price(pkg.getPrice())
                        .validityDays(pkg.getValidityDays())
                        .build())
                .toList();
    }

    @Transactional
    public PurchasedPackageResponse purchasePackage(PurchasePackageRequest request) {
        PackageCatalog packageCatalog = packageCatalogRepository.findById(request.getPackageCatalogId())
                .orElseThrow(() -> new ApiException("Package not found"));

        if (!paymentGateway.addPaymentCard(request.getCardToken())) {
            throw new ApiException("Mock add payment card failed");
        }
        if (!paymentGateway.paymentCharge(request.getCardToken(), "PKG-" + packageCatalog.getId() + "-" + System.nanoTime())) {
            throw new ApiException("Mock payment charge failed");
        }

        LocalDateTime now = LocalDateTime.now();
        PurchasedPackage purchasedPackage = PurchasedPackage.builder()
                .user(userService.getCurrentUser())
                .packageCatalog(packageCatalog)
                .totalCredits(packageCatalog.getCredits())
                .remainingCredits(packageCatalog.getCredits())
                .purchasedAt(now)
                .expiresAt(now.plusDays(packageCatalog.getValidityDays()))
                .status(PackageStatus.ACTIVE)
                .build();

        purchasedPackageRepository.save(purchasedPackage);
        creditLedgerService.record(purchasedPackage.getUser(), purchasedPackage, null, CreditTransactionType.PURCHASE,
                purchasedPackage.getTotalCredits(), purchasedPackage.getRemainingCredits(), "Package purchased");

        return toResponse(purchasedPackage);
    }

    @Transactional
    public List<PurchasedPackageResponse> getMyPackages() {
        LocalDateTime now = LocalDateTime.now();
        return purchasedPackageRepository.findByUserIdOrderByExpiresAtAsc(userService.getCurrentUser().getId()).stream()
                .peek(pkg -> refreshStatus(pkg, now))
                .map(this::toResponse)
                .toList();
    }

    public PurchasedPackage selectEligiblePackage(Long requestedPackageId, Long userId, String countryCode, Integer requiredCredits) {
        LocalDateTime now = LocalDateTime.now();
        if (requestedPackageId != null) {
            PurchasedPackage purchasedPackage = purchasedPackageRepository.findByIdAndUserId(requestedPackageId, userId)
                    .orElseThrow(() -> new ApiException("Purchased package not found"));
            validatePackageEligibility(purchasedPackage, countryCode, requiredCredits, now);
            return purchasedPackage;
        }

        return purchasedPackageRepository
                .findByUserIdAndStatusAndExpiresAtAfterAndRemainingCreditsGreaterThanOrderByExpiresAtAsc(
                        userId, PackageStatus.ACTIVE, now, requiredCredits - 1
                ).stream()
                .filter(pkg -> pkg.getPackageCatalog().getCountry().getCode().equalsIgnoreCase(countryCode))
                .findFirst()
                .orElseThrow(() -> new ApiException("No eligible package with sufficient credits"));
    }

    private void validatePackageEligibility(PurchasedPackage pkg, String countryCode, Integer requiredCredits, LocalDateTime now) {
        refreshStatus(pkg, now);
        if (pkg.getStatus() != PackageStatus.ACTIVE) {
            throw new ApiException("Selected package is not active");
        }
        if (!pkg.getPackageCatalog().getCountry().getCode().equalsIgnoreCase(countryCode)) {
            throw new ApiException("Package country does not match class country");
        }
        if (pkg.getRemainingCredits() < requiredCredits) {
            throw new ApiException("Insufficient credits");
        }
    }

    public void refreshStatus(PurchasedPackage purchasedPackage, LocalDateTime now) {
        if (!purchasedPackage.getExpiresAt().isAfter(now)) {
            purchasedPackage.setStatus(PackageStatus.EXPIRED);
            return;
        }
        if (purchasedPackage.getRemainingCredits() <= 0) {
            purchasedPackage.setStatus(PackageStatus.FULLY_USED);
            return;
        }
        purchasedPackage.setStatus(PackageStatus.ACTIVE);
    }

    public PurchasedPackageResponse toResponse(PurchasedPackage purchasedPackage) {
        return PurchasedPackageResponse.builder()
                .id(purchasedPackage.getId())
                .packageName(purchasedPackage.getPackageCatalog().getName())
                .countryCode(purchasedPackage.getPackageCatalog().getCountry().getCode())
                .totalCredits(purchasedPackage.getTotalCredits())
                .remainingCredits(purchasedPackage.getRemainingCredits())
                .purchasedAt(purchasedPackage.getPurchasedAt())
                .expiresAt(purchasedPackage.getExpiresAt())
                .status(purchasedPackage.getStatus())
                .build();
    }
}
