package abb.tech.ticket_service.service;

import abb.tech.ticket_service.config.RedisProperties;
import abb.tech.ticket_service.dto.event.PaymentFailedEvent;
import abb.tech.ticket_service.dto.event.PaymentSuccessEvent;
import abb.tech.ticket_service.enums.OrderStatus;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.service.impl.PaymentEventHandlerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerImplTest {

    @Mock private OrderService orderService;
    @Mock private TicketService ticketService;
    @Mock private PdfTicketService pdfTicketService;
    @Mock private EventSessionSeatService eventSessionSeatService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private RedisProperties redisProperties;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PaymentEventHandlerImpl paymentEventHandler;

    private static final Long ORDER_ID = 1L;
    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(ORDER_ID);
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        order.setUserId(100L);

        OrderItem item = new OrderItem();
        item.setPrice(BigDecimal.TEN);
        item.setEventSession(new EventSession());
        item.setSeat(new Seat());
        order.setOrderItems(List.of(item));

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisProperties.getPaymentIdempotencyKey()).thenReturn("payment:idemp:%d");
        lenient().when(redisProperties.getReservationKey()).thenReturn("res:%d:%d");
    }

    @Nested
    @DisplayName("handlePaymentSuccess Testləri")
    class SuccessEventTests {

        @Test
        @DisplayName("Uğurlu — Ödəniş uğurlu olduqda biletlər yaradılmalı və PDF göndərilməlidir")
        void handlePaymentSuccess_success() throws Exception {
            PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                    .orderId(ORDER_ID)
                    .userEmail("user@test.com")
                    .build();
            String message = objectMapper.writeValueAsString(event);

            when(redisTemplate.hasKey(anyString())).thenReturn(false);
            when(orderService.findById(ORDER_ID)).thenReturn(order);
            when(eventSessionSeatService.findByEventSessionIdAndSeatId(any(), any())).thenReturn(new EventSessionSeat());
            when(ticketService.getByIdWithDetails(any())).thenReturn(new Ticket());

            paymentEventHandler.handlePaymentSuccess(message);

            verify(orderService).create(argThat(o -> o.getOrderStatus() == OrderStatus.COMPLETED));
            verify(ticketService, atLeastOnce()).createTicket(any(Ticket.class));
            verify(pdfTicketService).generateAndSendTickets(anyList(), eq("user@test.com"), eq(order));
            verify(valueOperations).set(anyString(), eq("PROCESSED"), anyLong(), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("Idempotency — Eyni sifariş artıq işlənibsə proses dayanmalıdır")
        void handlePaymentSuccess_alreadyProcessed() throws Exception {
            PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                    .orderId(ORDER_ID)
                    .userEmail("user@test.com")
                    .build();
            String message = objectMapper.writeValueAsString(event);

            when(redisTemplate.hasKey(anyString())).thenReturn(true);

            paymentEventHandler.handlePaymentSuccess(message);

            verify(orderService, never()).findById(anyLong());
            verify(ticketService, never()).createTicket(any());
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed Testləri")
    class FailedEventTests {

        @Test
        @DisplayName("Uğurlu — Ödəniş uğursuz olduqda sifariş ləğv edilməlidir")
        void handlePaymentFailed_success() throws Exception {
            PaymentFailedEvent event = new PaymentFailedEvent();
            event.setOrderId(ORDER_ID);
            event.setReason("Insufficient funds");

            String message = objectMapper.writeValueAsString(event);

            paymentEventHandler.handlePaymentFailed(message);

            verify(orderService, timeout(1000)).cancelOrder(ORDER_ID);
        }

        @Test
        @DisplayName("Xəta — JSON oxunarkən xəta baş verərsə loglanıb dayanmalıdır")
        void handlePaymentFailed_deserializationError() {
            String invalidMessage = "{invalid-json}";

            paymentEventHandler.handlePaymentFailed(invalidMessage);

            verify(orderService, never()).cancelOrder(anyLong());
        }
    }

    @Nested
    @DisplayName("DLT (Dead Letter Topic) Testi")
    class DltTests {
        @Test
        @DisplayName("DLT Handler mesajı loglamalıdır")
        void handleDlt_success() {
            assertDoesNotThrow(() -> paymentEventHandler.handleDlt("message", "topic-retry-2"));
        }
    }
}