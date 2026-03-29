package com.testing.bookingsys.service;

import com.testing.bookingsys.dto.auth.AuthResponse;
import com.testing.bookingsys.dto.auth.ConfirmResetPasswordRequest;
import com.testing.bookingsys.dto.auth.LoginRequest;
import com.testing.bookingsys.dto.auth.RegisterRequest;
import com.testing.bookingsys.dto.auth.ResetPasswordRequest;
import com.testing.bookingsys.dto.auth.VerifyEmailRequest;
import com.testing.bookingsys.entity.AppUser;
import com.testing.bookingsys.enums.Role;
import com.testing.bookingsys.exception.ApiException;
import com.testing.bookingsys.integration.VerifyEmailGateway;
import com.testing.bookingsys.repository.UserRepository;
import com.testing.bookingsys.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final VerifyEmailGateway verifyEmailGateway;

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(user -> {
            throw new ApiException("Email already exists");
        });

        AppUser user = AppUser.builder()
                .email(request.getEmail().trim().toLowerCase())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .verified(false)
                .verificationToken(UUID.randomUUID().toString())
                .roles(Set.of(Role.ROLE_USER))
                .build();
        userRepository.save(user);

        if (!verifyEmailGateway.sendVerifyEmail(user.getEmail(), user.getVerificationToken())) {
            throw new ApiException("Mock email verification failed");
        }

        return Map.of(
                "message", "Registration successful. Verify email before login.",
                "verificationToken", user.getVerificationToken()
        );
    }

    @Transactional
    public Map<String, String> verifyEmail(VerifyEmailRequest request) {
        AppUser user = userRepository.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new ApiException("Invalid verification token"));
        user.setVerified(true);
        user.setVerificationToken(null);
        return Map.of("message", "Email verified successfully");
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        AppUser user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ApiException("Invalid credentials"));

        if (!user.isVerified()) {
            throw new ApiException("Email is not verified");
        }

        String token = jwtService.generateToken(
                User.withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                        .build(),
                Map.of("userId", user.getId(), "fullName", user.getFullName())
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Transactional
    public Map<String, Object> requestResetPassword(ResetPasswordRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ApiException("User not found"));
        user.setResetPasswordToken(UUID.randomUUID().toString());
        if (!verifyEmailGateway.sendVerifyEmail(user.getEmail(), user.getResetPasswordToken())) {
            throw new ApiException("Mock reset password email failed");
        }
        return Map.of(
                "message", "Reset password token generated",
                "resetToken", user.getResetPasswordToken()
        );
    }

    @Transactional
    public Map<String, String> confirmResetPassword(ConfirmResetPasswordRequest request) {
        AppUser user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new ApiException("Invalid reset password token"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        return Map.of("message", "Password reset successful");
    }
}
