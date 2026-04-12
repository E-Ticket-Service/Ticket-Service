package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.event.PaymentFailedEvent;
import abb.tech.ticket_service.dto.event.PaymentSuccessEvent;
import abb.tech.ticket_service.dto.event.RefundResultEvent;
import abb.tech.ticket_service.enums.OrderStatus;
import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.enums.TicketStatus;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static abb.tech.ticket_service.constant.KafkaConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventHandlerImpl implements PaymentEventHandler {

    private final OrderService orderService;
    private final TicketService ticketService;
    private final PdfTicketService pdfTicketService;
    private final EventSessionSeatService eventSessionSeatService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final abb.tech.ticket_service.config.RedisProperties redisProperties;
    private final ObjectMapper objectMapper;

    @Async
    @Override
    @Transactional
    @RetryableTopic(
            attempts = "3",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR
//            backOff = @BackOff(delay = 2000L, multiplier = 2.0)
    )
    @KafkaListener(topics = PAYMENT_SUCCESS_TOPIC, groupId = "${spring.kafka.consumer.group-id:" + TICKET_SERVICE_GROUP + "}")
    public void handlePaymentSuccess(String message) {
        PaymentSuccessEvent event;
        try {
            event = objectMapper.readValue(message, PaymentSuccessEvent.class);
        } catch (Exception e) {
            log.error("Error deserializing PaymentSuccessEvent: {}", message, e);
            return;
        }

        // Set SecurityContext for FeignClient and other services
        if (event.getUserId() != null) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    event.getUserId().toString(),
                    event.getUserEmail(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        try {
            log.info("Processing payment success event for order: {}", event.getOrderId());
            String idempotencyKey = String.format(redisProperties.getPaymentIdempotencyKey(), event.getOrderId());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(idempotencyKey))) {
                log.warn("Payment for order {} has already been processed (idempotency).", event.getOrderId());
                return;
            }

            Order order = orderService.findById(event.getOrderId());

            if (order.getOrderStatus() == OrderStatus.COMPLETED) {
                log.warn("Order {} is already completed. Skipping ticket creation.", order.getId());
                return;
            }

            order.setOrderStatus(OrderStatus.COMPLETED);
            order.setPaymentIntentId(event.getPaymentId());
            orderService.create(order);

            List<Ticket> createdTickets = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                EventSession session = item.getEventSession();
                Seat seat = item.getSeat();

                EventSessionSeat sessionSeat = eventSessionSeatService.findByEventSessionIdAndSeatId(session.getId(), seat.getId());

                sessionSeat.setSeatStatus(SeatStatus.SOLD);
                eventSessionSeatService.create(sessionSeat);

                String lockKey = String.format(redisProperties.getReservationKey(), session.getId(), seat.getId());
                redisTemplate.delete(lockKey);

                Ticket ticket = new Ticket();
                ticket.setTicketNumber(UUID.randomUUID());
                ticket.setUserId(order.getUserId());
                ticket.setOrder(order);
                ticket.setEventSession(session);
                ticket.setSeat(seat);
                ticket.setPrice(item.getPrice());
                ticket.setTicketStatus(TicketStatus.ACTIVE);

                ticketService.createTicket(ticket);

                Ticket fullTicket = ticketService.getByIdWithDetails(ticket.getId());
                createdTickets.add(fullTicket);
            }

            pdfTicketService.generateAndSendTickets(createdTickets, event.getUserEmail(), order);

            redisTemplate.opsForValue().set(idempotencyKey, "PROCESSED", redisProperties.getPaymentIdempotencyTtlHours(), TimeUnit.HOURS);

            log.info("Successfully processed order: {} and created {} tickets", order.getId(), createdTickets.size());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Async
    @Override
    @Transactional
    @RetryableTopic(
            attempts = "3",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR
//            backOff = @BackOff(delay = 2000L, multiplier = 2.0)
    )
    @KafkaListener(topics = PAYMENT_FAILED_TOPIC, groupId = "${spring.kafka.consumer.group-id:" + TICKET_SERVICE_GROUP + "}")
    public void handlePaymentFailed(String message) {
        PaymentFailedEvent event;
        try {
            event = objectMapper.readValue(message, PaymentFailedEvent.class);
        } catch (Exception e) {
            log.error("Error deserializing PaymentFailedEvent: {}", message, e);
            return;
        }

        // Set SecurityContext
        if (event.getUserId() != null) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    event.getUserId().toString(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        try {
            log.info("Processing payment failed event for order: {}. Reason: {}", event.getOrderId(), event.getReason());

            try {
                orderService.cancelOrder(event.getOrderId());
                log.info("Successfully cancelled order {} due to payment failure", event.getOrderId());
            } catch (Exception e) {
                log.error("Error cancelling order {} after payment failure", event.getOrderId(), e);
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Async
    @Override
    @Transactional
    @RetryableTopic(
            attempts = "3",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR
    )
    @KafkaListener(topics = REFUND_RESULT_TOPIC, groupId = "${spring.kafka.consumer.group-id:" + TICKET_SERVICE_GROUP + "}")
    public void handleRefundResult(String message) {
        RefundResultEvent event;
        try {
            event = objectMapper.readValue(message, RefundResultEvent.class);
        } catch (Exception e) {
            log.error("Error deserializing RefundResultEvent: {}", message, e);
            return;
        }

        if (!"SUCCESS".equals(event.getStatus())) {
            log.warn("Refund failed for order: {}. Reason: {}", event.getOrderId(), event.getReason());
            return;
        }

        log.info("Processing refund success event for order: {}", event.getOrderId());

        try {
            Order order = orderService.findById(event.getOrderId());
            
            // Set SecurityContext from order info if available
            if (order.getUserId() != null) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        order.getUserId().toString(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            try {
                order.setOrderStatus(OrderStatus.CANCELLED);
                orderService.create(order);

                List<Ticket> tickets = ticketService.findByOrderId(event.getOrderId());
                for (Ticket ticket : tickets) {
                    ticket.setTicketStatus(TicketStatus.CANCELLED);
                    ticketService.createTicket(ticket);

                    EventSessionSeat sessionSeat = eventSessionSeatService.findByEventSessionIdAndSeatId(
                            ticket.getEventSession().getId(),
                            ticket.getSeat().getId()
                    );
                    sessionSeat.setSeatStatus(SeatStatus.AVAILABLE);
                    eventSessionSeatService.create(sessionSeat);
                }
                log.info("Successfully processed refund for order: {} and cancelled {} tickets", event.getOrderId(), tickets.size());
            } finally {
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.error("Error processing refund for order: {}", event.getOrderId(), e);
        }
    }

    @DltHandler
    public void handleDlt(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Message from topic {} moved to DLT: {}", topic, message);
    }
}
