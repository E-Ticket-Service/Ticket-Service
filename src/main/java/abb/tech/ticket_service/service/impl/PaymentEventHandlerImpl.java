package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.event.PaymentSuccessEvent;
import abb.tech.ticket_service.enums.OrderStatus;
import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.enums.TicketStatus;
import abb.tech.ticket_service.model.Order;
import abb.tech.ticket_service.model.OrderItem;
import abb.tech.ticket_service.model.Seat;
import abb.tech.ticket_service.model.Ticket;
import abb.tech.ticket_service.service.OrderService;
import abb.tech.ticket_service.service.PaymentEventHandler;
import abb.tech.ticket_service.service.PdfTicketService;
import abb.tech.ticket_service.service.SeatService;
import abb.tech.ticket_service.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventHandlerImpl implements PaymentEventHandler {

    private final OrderService orderService;
    private final TicketService ticketService;
    private final SeatService seatService;
    private final PdfTicketService pdfTicketService;

    @Override
    @EventListener
    @Async
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Processing payment success event for order: {}", event.getOrderId());

        Order order = orderService.findById(event.getOrderId());

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            log.warn("Order {} is already completed. Skipping ticket creation.", order.getId());
            return;
        }

        order.setOrderStatus(OrderStatus.COMPLETED);
        orderService.save(order);

        List<Ticket> createdTickets = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            Seat seat = item.getSeat();
            seat.setSeatStatus(SeatStatus.SOLD);
            seatService.save(seat);

            Ticket ticket = new Ticket();
            ticket.setTicketNumber(UUID.randomUUID());
            ticket.setUserId(order.getUserId());
            ticket.setOrder(order);
            ticket.setEventSession(item.getEventSession());
            ticket.setSeat(seat);
            ticket.setPrice(item.getPrice());
            ticket.setTicketStatus(TicketStatus.ACTIVE);
            
            ticketService.createTicket(ticket);
            
            Ticket fullTicket = ticketService.getByIdWithDetails(ticket.getId());
            createdTickets.add(fullTicket);
        }
        
        pdfTicketService.generateAndSendTickets(createdTickets, event.getUserEmail(), order);
        
        log.info("Successfully processed order: {} and created {} tickets", order.getId(), createdTickets.size());
    }
}
