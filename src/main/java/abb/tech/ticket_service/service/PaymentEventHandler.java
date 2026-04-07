package abb.tech.ticket_service.service;

public interface PaymentEventHandler {
    void handlePaymentSuccess(String message);
    void handlePaymentFailed(String message);
}
