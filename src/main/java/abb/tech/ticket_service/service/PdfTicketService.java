package abb.tech.ticket_service.service;

import abb.tech.ticket_service.model.Order;
import abb.tech.ticket_service.model.Ticket;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface PdfTicketService {
    @Async
    void generateAndSendTickets(List<Ticket> tickets, String userEmail, Order order);
    
    byte[] generateTicketPdf(Ticket ticket);
}
