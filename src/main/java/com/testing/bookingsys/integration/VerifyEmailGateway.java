package com.testing.bookingsys.integration;

public interface VerifyEmailGateway {
    boolean sendVerifyEmail(String email, String token);
}
