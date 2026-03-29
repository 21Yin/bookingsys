package com.testing.bookingsys.integration;

public interface PaymentGateway {
    boolean addPaymentCard(String cardToken);
    boolean paymentCharge(String cardToken, String reference);
}
