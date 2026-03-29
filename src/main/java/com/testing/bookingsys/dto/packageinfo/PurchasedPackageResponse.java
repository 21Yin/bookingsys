package com.testing.bookingsys.dto.packageinfo;

import com.testing.bookingsys.enums.PackageStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PurchasedPackageResponse {
    private Long id;
    private String packageName;
    private String countryCode;
    private Integer totalCredits;
    private Integer remainingCredits;
    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;
    private PackageStatus status;
}
