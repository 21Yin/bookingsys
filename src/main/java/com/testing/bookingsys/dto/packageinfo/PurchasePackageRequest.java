package com.testing.bookingsys.dto.packageinfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchasePackageRequest {
    @NotNull
    private Long packageCatalogId;

    @NotBlank
    private String cardToken;
}
