package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.response.TicketResponse;
import abb.tech.ticket_service.model.Ticket;

import java.util.List;

public interface TicketService {
    Ticket getById(Long id);
    TicketResponse getTicketById(Long id);
    List<TicketResponse> getTicketsByUserId(Long userId);
    List<TicketResponse> getTicketsByOrderId(Long orderId);
    void cancelTicket(Long id);
    Ticket createTicket(Ticket ticket);
    byte[] generateTicketPdf(Long id);
    Ticket getByIdWithDetails(Long id);
}
