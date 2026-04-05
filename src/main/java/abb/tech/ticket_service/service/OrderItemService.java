package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.OrderItemCreationRequest;
import abb.tech.ticket_service.model.BucketItem;
import abb.tech.ticket_service.model.Order;
import abb.tech.ticket_service.model.OrderItem;
import java.util.List;

public interface OrderItemService {
    List<OrderItem> findByOrderId(Long orderId);
    OrderItem createOrderItem(Order order, OrderItemCreationRequest request);
    OrderItem createOrderItemFromBucket(Order order, BucketItem bucketItem);
}
