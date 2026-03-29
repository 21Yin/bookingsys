package com.testing.bookingsys.controller;

import com.testing.bookingsys.dto.schedule.BookClassRequest;
import com.testing.bookingsys.dto.schedule.BookingResponse;
import com.testing.bookingsys.dto.schedule.ScheduleResponse;
import com.testing.bookingsys.service.BookingService;
import com.testing.bookingsys.service.ScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final BookingService bookingService;

    @GetMapping("/schedules/{countryCode}")
    public ResponseEntity<List<ScheduleResponse>> listSchedules(@PathVariable String countryCode) {
        return ResponseEntity.ok(scheduleService.getSchedulesByCountry(countryCode));
    }

    @PostMapping("/schedules/{scheduleId}/book")
    public ResponseEntity<BookingResponse> bookClass(@PathVariable Long scheduleId, @RequestBody(required = false) BookClassRequest request) {
        return ResponseEntity.ok(bookingService.bookClass(scheduleId, request));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @PostMapping("/bookings/{bookingId}/check-in")
    public ResponseEntity<BookingResponse> checkIn(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.checkIn(bookingId));
    }

    @GetMapping("/bookings/me")
    public ResponseEntity<List<BookingResponse>> myBookings() {
        return ResponseEntity.ok(bookingService.myBookings());
    }
}
