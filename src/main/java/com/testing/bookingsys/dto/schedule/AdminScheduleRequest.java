package com.testing.bookingsys.dto.schedule;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminScheduleRequest {
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String countryCode;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    @Min(1)
    private Integer requiredCredits;

    @NotNull
    @Min(1)
    private Integer capacity;
}
