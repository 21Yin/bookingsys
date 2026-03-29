package com.testing.bookingsys.repository;

import com.testing.bookingsys.entity.Booking;
import com.testing.bookingsys.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndScheduleIdAndStatusIn(Long userId, Long scheduleId, List<BookingStatus> statuses);

    @Query("""
            select b from Booking b
            where b.user.id = :userId
              and b.status in :statuses
              and b.schedule.startTime < :endTime
              and b.schedule.endTime > :startTime
            """)
    List<Booking> findOverlappingBookings(Long userId, List<BookingStatus> statuses, LocalDateTime startTime, LocalDateTime endTime);

    long countByScheduleIdAndStatus(Long scheduleId, BookingStatus status);

    List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);

    List<Booking> findByScheduleIdAndStatusOrderByWaitlistPositionAscBookedAtAsc(Long scheduleId, BookingStatus status);

    @Query("""
            select b from Booking b
            where b.status = :status
              and b.schedule.endTime < :cutoff
            """)
    List<Booking> findAllEndedWaitlists(BookingStatus status, LocalDateTime cutoff);
}
