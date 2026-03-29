package com.testing.bookingsys.controller;

import com.testing.bookingsys.dto.auth.AuthResponse;
import com.testing.bookingsys.dto.auth.ConfirmResetPasswordRequest;
import com.testing.bookingsys.dto.auth.LoginRequest;
import com.testing.bookingsys.dto.auth.RegisterRequest;
import com.testing.bookingsys.dto.auth.ResetPasswordRequest;
import com.testing.bookingsys.dto.auth.VerifyEmailRequest;
import com.testing.bookingsys.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<Map<String, Object>> requestReset(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.requestResetPassword(request));
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<Map<String, String>> confirmReset(@Valid @RequestBody ConfirmResetPasswordRequest request) {
        return ResponseEntity.ok(authService.confirmResetPassword(request));
    }
}
