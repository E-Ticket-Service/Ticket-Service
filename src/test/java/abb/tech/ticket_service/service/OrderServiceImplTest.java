package abb.tech.ticket_service.service;

import abb.tech.ticket_service.config.RedisProperties;
import abb.tech.ticket_service.dto.request.OrderCreationRequest;
import abb.tech.ticket_service.dto.request.OrderItemCreationRequest;
import abb.tech.ticket_service.dto.response.OrderResponse;
import abb.tech.ticket_service.enums.OrderStatus;
import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.mapper.OrderMapper;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.repository.OrderRepository;
import abb.tech.ticket_service.service.impl.OrderServiceImpl;
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
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemService orderItemService;
    @Mock private BucketService bucketService;
    @Mock private OrderMapper orderMapper;
    @Mock private EventSessionSeatService eventSessionSeatService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private RedisProperties redisProperties;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        order.setOrderItems(new ArrayList<>());
    }

    @Nested
    @DisplayName("createOrder Testləri")
    class CreateOrderTests {

        @Test
        @DisplayName("Uğurlu — Sifariş yaradılmalı, Kafka-ya event göndərilməlidir")
        void createOrder_success() throws Exception {
            OrderCreationRequest request = OrderCreationRequest.builder()
                    .userId(USER_ID)
                    .orderItems(List.of(new OrderItemCreationRequest()))
                    .build();

            OrderItem item = new OrderItem();
            item.setPrice(BigDecimal.valueOf(50));

            when(orderMapper.toEntity(request)).thenReturn(order);
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderItemService.createOrderItem(any(), any())).thenReturn(item);
            when(orderMapper.toResponse(any())).thenReturn(mock(OrderResponse.class));

            OrderResponse response = orderService.createOrder(request);

            assertNotNull(response);
            verify(kafkaTemplate).send(any(), anyString());
            verify(orderRepository, atLeastOnce()).save(order);
        }
    }

    @Nested
    @DisplayName("createOrderFromBucket Testləri")
    class BucketOrderTests {

        @Test
        @DisplayName("Xəta — Səbət boşdursa IllegalStateException atmalıdır")
        void createOrderFromBucket_emptyBucket() {
            Bucket bucket = new Bucket();
            bucket.setId(1L);
            when(bucketService.getBucketEntityByUserId(USER_ID)).thenReturn(bucket);
            when(bucketService.getSelectedBucketItemsByBucketId(1L)).thenReturn(List.of());

            assertThrows(IllegalStateException.class, () -> orderService.createOrderFromBucket(USER_ID));
        }
    }

    @Nested
    @DisplayName("cancelOrder Testləri")
    class CancelOrderTests {

        @Test
        @DisplayName("Uğurlu — Sifariş ləğv edilməli, Redis kilidi silinməlidir")
        void cancelOrder_success() {
            EventSession session = new EventSession(); session.setId(10L);
            Seat seat = new Seat(); seat.setId(20L);

            OrderItem item = new OrderItem();
            item.setEventSession(session);
            item.setSeat(seat);
            order.setOrderItems(List.of(item));

            EventSessionSeat sessionSeat = new EventSessionSeat();
            sessionSeat.setSeatStatus(SeatStatus.RESERVED);

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(eventSessionSeatService.findByEventSessionIdAndSeatId(10L, 20L)).thenReturn(sessionSeat);
            lenient().when(redisProperties.getReservationKey()).thenReturn("lock:%d:%d");

            orderService.cancelOrder(ORDER_ID);

            assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
            assertEquals(SeatStatus.AVAILABLE, sessionSeat.getSeatStatus());
            verify(redisTemplate).delete(anyString());
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Xəta — Tamamlanmış (COMPLETED) sifariş ləğv edilə bilməz")
        void cancelOrder_alreadyCompleted() {
            order.setOrderStatus(OrderStatus.COMPLETED);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(ORDER_ID));
        }
    }

    @Nested
    @DisplayName("Yardımçı Metodlar")
    class HelperTests {
        @Test
        @DisplayName("findById — Tapılmadıqda NotFoundException")
        void findById_notFound() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> orderService.findById(ORDER_ID));
        }
    }
}