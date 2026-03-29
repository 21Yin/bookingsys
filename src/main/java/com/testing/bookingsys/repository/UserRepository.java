package com.testing.bookingsys.repository;

import com.testing.bookingsys.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    Optional<AppUser> findByVerificationToken(String verificationToken);
    Optional<AppUser> findByResetPasswordToken(String resetPasswordToken);
}
