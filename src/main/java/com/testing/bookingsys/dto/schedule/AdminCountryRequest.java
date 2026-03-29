package com.testing.bookingsys.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCountryRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String name;
}
