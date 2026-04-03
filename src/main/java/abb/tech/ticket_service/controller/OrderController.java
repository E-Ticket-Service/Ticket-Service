package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.OrderCreationRequest;
import abb.tech.ticket_service.dto.event.PaymentSuccessEvent;
import abb.tech.ticket_service.dto.response.OrderResponse;
import abb.tech.ticket_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderCreationRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PostMapping("/bucket/{userId}")
    public ResponseEntity<OrderResponse> createOrderFromBucket(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.createOrderFromBucket(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

}
