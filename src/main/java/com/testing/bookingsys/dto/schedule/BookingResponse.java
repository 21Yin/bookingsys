package com.testing.bookingsys.dto.schedule;

import com.testing.bookingsys.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookingResponse {
    private Long id;
    private Long scheduleId;
    private String scheduleTitle;
    private String countryCode;
    private BookingStatus status;
    private Integer creditsCharged;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer waitlistPosition;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkedInAt;
}
