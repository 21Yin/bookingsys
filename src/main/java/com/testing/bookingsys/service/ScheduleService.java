package com.testing.bookingsys.service;

import com.testing.bookingsys.dto.schedule.ScheduleResponse;
import com.testing.bookingsys.entity.ClassSchedule;
import com.testing.bookingsys.enums.BookingStatus;
import com.testing.bookingsys.exception.ApiException;
import com.testing.bookingsys.repository.BookingRepository;
import com.testing.bookingsys.repository.ClassScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ClassScheduleRepository classScheduleRepository;
    private final BookingRepository bookingRepository;

    public List<ScheduleResponse> getSchedulesByCountry(String countryCode) {
        return classScheduleRepository.findByCountryCodeIgnoreCaseAndStartTimeAfterOrderByStartTimeAsc(countryCode, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ClassSchedule getScheduleForBooking(Long scheduleId) {
        return classScheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ApiException("Schedule not found"));
    }

    public ScheduleResponse toResponse(ClassSchedule schedule) {
        long booked = bookingRepository.countByScheduleIdAndStatus(schedule.getId(), BookingStatus.BOOKED);
        long waitlisted = bookingRepository.countByScheduleIdAndStatus(schedule.getId(), BookingStatus.WAITLISTED);
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .countryCode(schedule.getCountry().getCode())
                .countryName(schedule.getCountry().getName())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .requiredCredits(schedule.getRequiredCredits())
                .capacity(schedule.getCapacity())
                .bookedSlots(booked)
                .waitlistCount(waitlisted)
                .remainingSlots(Math.max(0, schedule.getCapacity() - booked))
                .build();
    }
}
