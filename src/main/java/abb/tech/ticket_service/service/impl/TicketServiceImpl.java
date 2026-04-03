package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.response.TicketResponse;
import abb.tech.ticket_service.enums.TicketStatus;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.mapper.TicketMapper;
import abb.tech.ticket_service.model.Ticket;
import abb.tech.ticket_service.repository.TicketRepository;
import abb.tech.ticket_service.service.PdfTicketService;
import abb.tech.ticket_service.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final PdfTicketService pdfTicketService;

    @Override
    @Transactional(readOnly = true)
    public Ticket getById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = getById(id);
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByUserId(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByOrderId(Long orderId) {
        return ticketRepository.findByOrderId(orderId).stream()
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelTicket(Long id) {
        Ticket ticket = getById(id);
        ticket.setTicketStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public byte[] generateTicketPdf(Long id) {
        Ticket ticket = getByIdWithDetails(id);
        return pdfTicketService.generateTicketPdf(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getByIdWithDetails(Long id) {
        return ticketRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
    }
}
