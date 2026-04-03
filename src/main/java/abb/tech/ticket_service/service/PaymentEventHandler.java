package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.event.PaymentSuccessEvent;

public interface PaymentEventHandler {
    void handlePaymentSuccess(PaymentSuccessEvent event);
}
