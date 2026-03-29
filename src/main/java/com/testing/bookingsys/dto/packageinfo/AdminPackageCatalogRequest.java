package com.testing.bookingsys.dto.packageinfo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminPackageCatalogRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String countryCode;

    @NotNull
    @Min(1)
    private Integer credits;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer validityDays;
}
