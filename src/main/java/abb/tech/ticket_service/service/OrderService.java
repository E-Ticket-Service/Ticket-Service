package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.OrderCreationRequest;
import abb.tech.ticket_service.dto.response.OrderResponse;
import abb.tech.ticket_service.model.Order;

import java.util.List;

public interface OrderService {
    Order findById(Long id);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getOrdersByUserId(Long userId);
    Order save(Order order);
    OrderResponse createOrder(OrderCreationRequest request);
    OrderResponse createOrderFromBucket(Long userId);
    void cancelOrder(Long id);
}
