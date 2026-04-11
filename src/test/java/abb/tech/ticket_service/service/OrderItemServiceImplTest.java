package abb.tech.ticket_service.service;

import abb.tech.ticket_service.config.RedisProperties;
import abb.tech.ticket_service.dto.request.OrderItemCreationRequest;
import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.repository.OrderItemRepository;
import abb.tech.ticket_service.service.impl.OrderItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock private OrderItemRepository orderItemRepository;
    @Mock private EventSessionService eventSessionService;
    @Mock private SeatService seatService;
    @Mock private EventSessionSeatService eventSessionSeatService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private RedisProperties redisProperties;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private Order order;
    private EventSession session;
    private Seat seat;
    private EventSessionSeat sessionSeat;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setUserId(1L);

        session = new EventSession();
        session.setId(10L);

        seat = new Seat();
        seat.setId(20L);

        sessionSeat = new EventSessionSeat();
        sessionSeat.setEventSession(session);
        sessionSeat.setSeat(seat);
        sessionSeat.setPrice(BigDecimal.TEN);
        sessionSeat.setSeatStatus(SeatStatus.AVAILABLE);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisProperties.getReservationKey()).thenReturn("reserve:%d:%d");
        lenient().when(redisProperties.getReservationTtlMinutes()).thenReturn(15L);
    }

    @Nested
    @DisplayName("OrderItem Yaradılması Testləri")
    class CreationTests {

        @Test
        @DisplayName("Uğurlu — OrderItem yaradılmalı və Redis-də kilid qoyulmalıdır")
        void createOrderItem_success() {
            OrderItemCreationRequest request = new OrderItemCreationRequest();
            request.setEventSessionId(10L);
            request.setSeatId(20L);

            when(eventSessionService.findById(10L)).thenReturn(session);
            when(seatService.getById(20L)).thenReturn(seat);
            when(eventSessionSeatService.findByEventSessionIdAndSeatId(10L, 20L)).thenReturn(sessionSeat);
            when(redisTemplate.hasKey(anyString())).thenReturn(false);
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArguments()[0]);

            OrderItem result = orderItemService.createOrderItem(order, request);

            assertNotNull(result);
            assertEquals(SeatStatus.RESERVED, sessionSeat.getSeatStatus());
            verify(valueOperations).set(anyString(), eq("1"), eq(15L), eq(TimeUnit.MINUTES));
            verify(orderItemRepository).save(any(OrderItem.class));
        }

        @Test
        @DisplayName("Xəta — Yer artıq Redis-də rezerv olunubsa IllegalStateException atmalıdır")
        void createOrderItem_alreadyReservedInRedis() {
            OrderItemCreationRequest request = new OrderItemCreationRequest();
            when(eventSessionService.findById(any())).thenReturn(session);
            when(seatService.getById(any())).thenReturn(seat);
            when(eventSessionSeatService.findByEventSessionIdAndSeatId(any(), any())).thenReturn(sessionSeat);
            when(redisTemplate.hasKey(anyString())).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> orderItemService.createOrderItem(order, request));
            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Xəta — Yerin statusu AVAILABLE deyilsə IllegalStateException atmalıdır")
        void createOrderItem_notAvailableStatus() {
            sessionSeat.setSeatStatus(SeatStatus.SOLD);
            OrderItemCreationRequest request = new OrderItemCreationRequest();

            when(eventSessionService.findById(any())).thenReturn(session);
            when(seatService.getById(any())).thenReturn(seat);
            when(eventSessionSeatService.findByEventSessionIdAndSeatId(any(), any())).thenReturn(sessionSeat);
            when(redisTemplate.hasKey(anyString())).thenReturn(false);

            assertThrows(IllegalStateException.class, () -> orderItemService.createOrderItem(order, request));
        }
    }

    @Nested
    @DisplayName("Səbətdən (Bucket) Yaradılma")
    class BucketCreationTests {

        @Test
        @DisplayName("Uğurlu — BucketItem-dən OrderItem yaradılmalıdır")
        void createOrderItemFromBucket_success() {
            BucketItem bucketItem = new BucketItem();
            bucketItem.setEventSession(session);
            bucketItem.setSeat(seat);

            when(eventSessionSeatService.findByEventSessionIdAndSeatId(any(), any())).thenReturn(sessionSeat);
            when(redisTemplate.hasKey(anyString())).thenReturn(false);
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArguments()[0]);

            OrderItem result = orderItemService.createOrderItemFromBucket(order, bucketItem);

            assertNotNull(result);
            verify(eventSessionSeatService).create(sessionSeat);
        }
    }

    @Nested
    @DisplayName("Axtarış Testləri")
    class RetrievalTests {

        @Test
        @DisplayName("Uğurlu — OrderId-yə görə siyahı qaytarmalıdır")
        void findByOrderId_success() {
            when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(new OrderItem()));

            List<OrderItem> results = orderItemService.findByOrderId(1L);

            assertEquals(1, results.size());
            verify(orderItemRepository).findByOrderId(1L);
        }
    }
}