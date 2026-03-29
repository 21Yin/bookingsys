package com.testing.bookingsys.repository;

import com.testing.bookingsys.entity.ClassSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCountryCodeIgnoreCaseAndStartTimeAfterOrderByStartTimeAsc(String countryCode, LocalDateTime startTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cs from ClassSchedule cs where cs.id = :id")
    Optional<ClassSchedule> findByIdForUpdate(Long id);

    List<ClassSchedule> findByEndTimeBefore(LocalDateTime endTime);
}
