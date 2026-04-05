package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.OrderCreationRequest;
import abb.tech.ticket_service.dto.request.OrderItemCreationRequest;
import abb.tech.ticket_service.dto.response.OrderResponse;
import abb.tech.ticket_service.enums.OrderStatus;
import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.mapper.OrderMapper;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.repository.OrderRepository;
import abb.tech.ticket_service.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final BucketService bucketService;
    private final SeatService seatService;
    private final OrderMapper orderMapper;
    private final EventSessionSeatService eventSessionSeatService;

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
        orderRepository.save(order);

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
        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = findById(id);
        
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }
        
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
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
            }
        }
    }
}
