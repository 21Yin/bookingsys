package com.testing.bookingsys.dto.schedule;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduleResponse {
    private Long id;
    private String title;
    private String description;
    private String countryCode;
    private String countryName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer requiredCredits;
    private Integer capacity;
    private Long bookedSlots;
    private Long waitlistCount;
    private Long remainingSlots;
}
