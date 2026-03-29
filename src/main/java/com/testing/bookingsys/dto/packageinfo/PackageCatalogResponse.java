package com.testing.bookingsys.dto.packageinfo;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PackageCatalogResponse {
    private Long id;
    private String name;
    private String countryCode;
    private String countryName;
    private Integer credits;
    private BigDecimal price;
    private Integer validityDays;
}
