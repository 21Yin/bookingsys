package com.testing.bookingsys.repository;

import com.testing.bookingsys.entity.PurchasedPackage;
import com.testing.bookingsys.enums.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchasedPackageRepository extends JpaRepository<PurchasedPackage, Long> {
    List<PurchasedPackage> findByUserIdOrderByExpiresAtAsc(Long userId);

    List<PurchasedPackage> findByUserIdAndStatusAndExpiresAtAfterAndRemainingCreditsGreaterThanOrderByExpiresAtAsc(
            Long userId,
            PackageStatus status,
            LocalDateTime now,
            Integer remainingCredits
    );

    Optional<PurchasedPackage> findByIdAndUserId(Long id, Long userId);
}
