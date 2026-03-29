package com.testing.bookingsys.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
