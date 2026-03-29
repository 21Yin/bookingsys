package com.testing.bookingsys.service;

import com.testing.bookingsys.dto.schedule.BookClassRequest;
import com.testing.bookingsys.dto.schedule.BookingResponse;
import com.testing.bookingsys.entity.AppUser;
import com.testing.bookingsys.entity.Booking;
import com.testing.bookingsys.entity.ClassSchedule;
import com.testing.bookingsys.entity.PurchasedPackage;
import com.testing.bookingsys.enums.BookingStatus;
import com.testing.bookingsys.enums.CreditTransactionType;
import com.testing.bookingsys.exception.ApiException;
import com.testing.bookingsys.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleService scheduleService;
    private final UserService userService;
    private final PackageService packageService;
    private final CreditLedgerService creditLedgerService;
    private final BookingLockService bookingLockService;

    @Transactional
    public BookingResponse bookClass(Long scheduleId, BookClassRequest request) {
        AppUser user = userService.getCurrentUser();
        String lockToken = bookingLockService.acquire("schedule-" + scheduleId);
        try {
            ClassSchedule schedule = scheduleService.getScheduleForBooking(scheduleId);

            if (schedule.getStartTime().isBefore(LocalDateTime.now())) {
                throw new ApiException("Cannot book a class that already started");
            }
            if (bookingRepository.existsByUserIdAndScheduleIdAndStatusIn(
                    user.getId(), scheduleId, List.of(BookingStatus.BOOKED, BookingStatus.WAITLISTED))) {
                throw new ApiException("User already booked or waitlisted for this class");
            }
            if (!bookingRepository.findOverlappingBookings(
                    user.getId(),
                    List.of(BookingStatus.BOOKED),
                    schedule.getStartTime(),
                    schedule.getEndTime()
            ).isEmpty()) {
                throw new ApiException("User has another class during the same timeslot");
            }

            PurchasedPackage purchasedPackage = packageService.selectEligiblePackage(
                    request == null ? null : request.getPurchasedPackageId(),
                    user.getId(),
                    schedule.getCountry().getCode(),
                    schedule.getRequiredCredits()
            );
            purchasedPackage.setRemainingCredits(purchasedPackage.getRemainingCredits() - schedule.getRequiredCredits());
            packageService.refreshStatus(purchasedPackage, LocalDateTime.now());

            long bookedCount = bookingRepository.countByScheduleIdAndStatus(scheduleId, BookingStatus.BOOKED);
            BookingStatus status = bookedCount < schedule.getCapacity() ? BookingStatus.BOOKED : BookingStatus.WAITLISTED;
            Integer waitlistPosition = null;
            if (status == BookingStatus.WAITLISTED) {
                waitlistPosition = bookingRepository.findByScheduleIdAndStatusOrderByWaitlistPositionAscBookedAtAsc(
                        scheduleId, BookingStatus.WAITLISTED
                ).size() + 1;
            }

            Booking booking = bookingRepository.save(Booking.builder()
                    .user(user)
                    .schedule(schedule)
                    .purchasedPackage(purchasedPackage)
                    .status(status)
                    .creditsCharged(schedule.getRequiredCredits())
                    .bookedAt(LocalDateTime.now())
                    .waitlistPosition(waitlistPosition)
                    .build());

            creditLedgerService.record(
                    user,
                    purchasedPackage,
                    schedule,
                    CreditTransactionType.BOOKING_DEBIT,
                    -schedule.getRequiredCredits(),
                    purchasedPackage.getRemainingCredits(),
                    status == BookingStatus.BOOKED ? "Class booked" : "Class waitlisted with credit hold"
            );

            return toResponse(booking);
        } finally {
            bookingLockService.release("schedule-" + scheduleId, lockToken);
        }
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        AppUser user = userService.getCurrentUser();
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new ApiException("Booking not found"));

        BookingStatus originalStatus = booking.getStatus();
        if (!(originalStatus == BookingStatus.BOOKED || originalStatus == BookingStatus.WAITLISTED)) {
            throw new ApiException("Booking is not cancellable");
        }

        String lockToken = bookingLockService.acquire("schedule-" + booking.getSchedule().getId());
        try {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(LocalDateTime.now());

            if (shouldRefundOnCancel(originalStatus, booking)) {
                refundCredits(booking, CreditTransactionType.CANCELLATION_REFUND, "Booking cancelled and refunded");
            }

            if (originalStatus == BookingStatus.BOOKED) {
                promoteWaitlist(booking.getSchedule().getId());
            }
            return toResponse(booking);
        } finally {
            bookingLockService.release("schedule-" + booking.getSchedule().getId(), lockToken);
        }
    }

    @Transactional
    public BookingResponse checkIn(Long bookingId) {
        AppUser user = userService.getCurrentUser();
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new ApiException("Booking not found"));
        LocalDateTime now = LocalDateTime.now();
        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new ApiException("Only booked classes can be checked in");
        }
        if (now.isBefore(booking.getSchedule().getStartTime()) || now.isAfter(booking.getSchedule().getEndTime())) {
            throw new ApiException("Check-in is allowed only during class time");
        }
        booking.setStatus(BookingStatus.ATTENDED);
        booking.setCheckedInAt(now);
        return toResponse(booking);
    }

    public List<BookingResponse> myBookings() {
        return bookingRepository.findByUserIdOrderByBookedAtDesc(userService.getCurrentUser().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void refundExpiredWaitlists() {
        bookingRepository.findAllEndedWaitlists(BookingStatus.WAITLISTED, LocalDateTime.now()).forEach(booking -> {
            booking.setStatus(BookingStatus.WAITLIST_EXPIRED);
            refundCredits(booking, CreditTransactionType.WAITLIST_REFUND, "Waitlist expired after class end");
        });
    }

    private boolean shouldRefundOnCancel(BookingStatus originalStatus, Booking booking) {
        if (originalStatus == BookingStatus.WAITLISTED) {
            return true;
        }
        long hoursDifference = Duration.between(LocalDateTime.now(), booking.getSchedule().getStartTime()).toHours();
        return hoursDifference >= 4;
    }

    private void promoteWaitlist(Long scheduleId) {
        bookingRepository.findByScheduleIdAndStatusOrderByWaitlistPositionAscBookedAtAsc(scheduleId, BookingStatus.WAITLISTED)
                .stream()
                .findFirst()
                .ifPresent(waitlisted -> {
                    waitlisted.setStatus(BookingStatus.BOOKED);
                    waitlisted.setWaitlistPosition(null);
                });
    }

    private void refundCredits(Booking booking, CreditTransactionType transactionType, String remarks) {
        PurchasedPackage purchasedPackage = booking.getPurchasedPackage();
        purchasedPackage.setRemainingCredits(purchasedPackage.getRemainingCredits() + booking.getCreditsCharged());
        packageService.refreshStatus(purchasedPackage, LocalDateTime.now());
        creditLedgerService.record(
                booking.getUser(),
                purchasedPackage,
                booking.getSchedule(),
                transactionType,
                booking.getCreditsCharged(),
                purchasedPackage.getRemainingCredits(),
                remarks
        );
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .scheduleId(booking.getSchedule().getId())
                .scheduleTitle(booking.getSchedule().getTitle())
                .countryCode(booking.getSchedule().getCountry().getCode())
                .status(booking.getStatus())
                .creditsCharged(booking.getCreditsCharged())
                .startTime(booking.getSchedule().getStartTime())
                .endTime(booking.getSchedule().getEndTime())
                .waitlistPosition(booking.getWaitlistPosition())
                .bookedAt(booking.getBookedAt())
                .cancelledAt(booking.getCancelledAt())
                .checkedInAt(booking.getCheckedInAt())
                .build();
    }
}
