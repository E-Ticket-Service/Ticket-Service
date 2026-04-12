package abb.tech.ticket_service.service.impl;

import static abb.tech.ticket_service.constant.KafkaConstants.ORDER_CREATED_TOPIC;
import static abb.tech.ticket_service.constant.KafkaConstants.REFUND_REQUEST_TOPIC;
import abb.tech.ticket_service.client.UserClient;
import abb.tech.ticket_service.dto.event.OrderCreatedEvent;
import abb.tech.ticket_service.dto.event.RefundRequestEvent;
import abb.tech.ticket_service.dto.request.OrderCreationRequest;
import abb.tech.ticket_service.dto.request.OrderItemCreationRequest;
import abb.tech.ticket_service.dto.response.OrderResponse;
import abb.tech.ticket_service.dto.response.UserResponse;
import abb.tech.ticket_service.enums.OrderStatus;
import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.mapper.OrderMapper;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.repository.OrderRepository;
import abb.tech.ticket_service.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final BucketService bucketService;
    private final OrderMapper orderMapper;
    private final EventSessionSeatService eventSessionSeatService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final abb.tech.ticket_service.config.RedisProperties redisProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserClient userClient;

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreationRequest request) {
        Order order = orderMapper.toEntity(request);
        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemCreationRequest itemReq : request.getOrderItems()) {
            OrderItem orderItem = orderItemService.createOrderItem(order, itemReq);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getPrice());
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);
        order = orderRepository.save(order);

        sendOrderCreatedEvent(order);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse createOrderFromBucket(Long userId) {
        Bucket bucket = bucketService.getBucketEntityByUserId(userId);

        List<BucketItem> selectedItems = bucketService.getSelectedBucketItemsByBucketId(bucket.getId());

        if (selectedItems.isEmpty()) {
            throw new IllegalStateException("No items selected in bucket");
        }

        Order order = orderMapper.toEntity(OrderCreationRequest.builder().userId(userId).build());
        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (BucketItem bucketItem : selectedItems) {
            OrderItem orderItem = orderItemService.createOrderItemFromBucket(order, bucketItem);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getPrice());
            
            bucketService.deleteBucketItem(bucketItem);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);
        order = orderRepository.save(order);

        sendOrderCreatedEvent(order);

        return orderMapper.toResponse(order);
    }

    private void sendOrderCreatedEvent(Order order) {
        UserResponse user;
        try {
            user = userClient.findById(order.getUserId());
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve user info for order " + order.getId() + ": " + e.getMessage(), e);
        }

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .userEmail(user.getEmail())
                .totalAmount(order.getTotalAmount())
                .currency("azn")
                .paymentMethod("card")
                .build();
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ORDER_CREATED_TOPIC, jsonEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing OrderCreatedEvent", e);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = findById(id);
        
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return;
        }
        validateCancellationEligibility(order);

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            sendRefundRequest(order);
            return;
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        for (OrderItem item : order.getOrderItems()) {
            EventSessionSeat sessionSeat = eventSessionSeatService.findByEventSessionIdAndSeatId(
                    item.getEventSession().getId(), 
                    item.getSeat().getId()
            );

            if (sessionSeat.getSeatStatus() == SeatStatus.RESERVED) {
                sessionSeat.setSeatStatus(SeatStatus.AVAILABLE);
                eventSessionSeatService.create(sessionSeat);

                String lockKey = String.format(redisProperties.getReservationKey(), item.getEventSession().getId(), item.getSeat().getId());
                redisTemplate.delete(lockKey);
            }
        }
    }

    private void validateCancellationEligibility(Order order) {
        LocalDateTime now = LocalDateTime.now();

        if (order.getCreatedAt().plusHours(24).isBefore(now)) {
            throw new IllegalStateException("Sifariş verildikdən sonra 24 saat keçdiyi üçün ləğv edilə bilməz.");
        }

    }

    private void sendRefundRequest(Order order) {
        if (order.getPaymentIntentId() == null) {
            throw new IllegalStateException("Cannot refund order without payment intent ID");
        }

        RefundRequestEvent event = RefundRequestEvent.builder()
                .paymentIntentId(order.getPaymentIntentId())
                .amount(order.getTotalAmount())
                .orderId(order.getId())
                .build();

        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(REFUND_REQUEST_TOPIC, jsonEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing RefundRequestEvent", e);
        }
    }
}
