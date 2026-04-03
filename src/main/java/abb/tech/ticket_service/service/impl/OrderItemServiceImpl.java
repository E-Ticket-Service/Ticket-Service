package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.OrderItemCreationRequest;
import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.repository.OrderItemRepository;
import abb.tech.ticket_service.service.EventSessionService;
import abb.tech.ticket_service.service.OrderItemService;
import abb.tech.ticket_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final EventSessionService eventSessionService;
    private final SeatService seatService;
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }
    
    @Override
    @Transactional
    public OrderItem createOrderItem(Order order, OrderItemCreationRequest request) {
        EventSession session = eventSessionService.findById(request.getEventSessionId());
        Seat seat = seatService.getById(request.getSeatId());

        checkSeatStatus(seat);

        seat.setSeatStatus(SeatStatus.RESERVED);
        seatService.save(seat);

        BigDecimal price = calculatePrice(session, seat);

        return saveOrderItem(order, session, seat, price);
    }

    @Override
    @Transactional
    public OrderItem createOrderItemFromBucket(Order order, BucketItem bucketItem) {
        EventSession session = bucketItem.getEventSession();
        Seat seat = bucketItem.getSeat();

        checkSeatStatus(seat);

        seat.setSeatStatus(SeatStatus.RESERVED);
        seatService.save(seat);

        BigDecimal price = calculatePrice(session, seat);

        return saveOrderItem(order, session, seat, price);
    }

    private static void checkSeatStatus(Seat seat) {
        if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + seat.getId() + " is not available");
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

    private BigDecimal calculatePrice(EventSession session, Seat seat) {
        return session.getBasePrice().add(seat.getExtraPrice() != null ? seat.getExtraPrice() : BigDecimal.ZERO);
    }
}
