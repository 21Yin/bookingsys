package com.testing.bookingsys.integration;

import org.springframework.stereotype.Service;

@Service
public class MockGatewayService implements PaymentGateway, VerifyEmailGateway {

    @Override
    public boolean addPaymentCard(String cardToken) {
        return !cardToken.toUpperCase().contains("FAIL");
    }

    @Override
    public boolean paymentCharge(String cardToken, String reference) {
        return !cardToken.toUpperCase().contains("FAIL") && !reference.toUpperCase().contains("FAIL");
    }

    @Override
    public boolean sendVerifyEmail(String email, String token) {
        return !email.toUpperCase().contains("FAIL") && token != null;
    }
}
