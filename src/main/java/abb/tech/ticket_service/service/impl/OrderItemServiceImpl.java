package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.OrderItemCreationRequest;
import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.service.EventSessionSeatService;
import abb.tech.ticket_service.service.EventSessionService;
import abb.tech.ticket_service.service.OrderItemService;
import abb.tech.ticket_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final abb.tech.ticket_service.repository.OrderItemRepository orderItemRepository;
    private final EventSessionService eventSessionService;
    private final SeatService seatService;
    private final EventSessionSeatService eventSessionSeatService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final abb.tech.ticket_service.config.RedisProperties redisProperties;

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public OrderItem createOrderItem(Order order, OrderItemCreationRequest request) {
        EventSession session = eventSessionService.findById(request.getEventSessionId());
        Seat seat = seatService.getById(request.getSeatId());

        return processOrderItemCreation(order, session, seat);
    }

    @Override
    @Transactional
    public OrderItem createOrderItemFromBucket(Order order, BucketItem bucketItem) {
        EventSession session = bucketItem.getEventSession();
        Seat seat = bucketItem.getSeat();

        return processOrderItemCreation(order, session, seat);
    }

    private OrderItem processOrderItemCreation(Order order, EventSession session, Seat seat) {
        EventSessionSeat sessionSeat = eventSessionSeatService.findByEventSessionIdAndSeatId(session.getId(), seat.getId());

        checkSeatStatus(sessionSeat);

        String lockKey = String.format(redisProperties.getReservationKey(), session.getId(), seat.getId());
        redisTemplate.opsForValue().set(lockKey, order.getUserId().toString(), redisProperties.getReservationTtlMinutes(), TimeUnit.MINUTES);

        sessionSeat.setSeatStatus(SeatStatus.RESERVED);
        eventSessionSeatService.create(sessionSeat);

        return saveOrderItem(order, session, seat, sessionSeat.getPrice());
    }

    private void checkSeatStatus(EventSessionSeat sessionSeat) {

        String lockKey = String.format(redisProperties.getReservationKey(), sessionSeat.getEventSession().getId(), sessionSeat.getSeat().getId());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new IllegalStateException("Seat is already reserved in another process");
        }

        if (sessionSeat.getSeatStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat is not available for this session");
        }
    }

    private OrderItem saveOrderItem(Order order, EventSession session, Seat seat, BigDecimal price) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setEventSession(session);
        orderItem.setSeat(seat);
        orderItem.setPrice(price);

        return orderItemRepository.save(orderItem);
    }
}
