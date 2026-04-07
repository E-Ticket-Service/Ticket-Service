package abb.tech.ticket_service.controller;

import static abb.tech.ticket_service.constant.KafkaConstants.*;
import abb.tech.ticket_service.dto.event.PaymentFailedEvent;
import abb.tech.ticket_service.dto.event.PaymentSuccessEvent;
import abb.tech.ticket_service.dto.request.OrderCreationRequest;
import abb.tech.ticket_service.dto.response.OrderResponse;
import abb.tech.ticket_service.service.OrderService;
import abb.tech.ticket_service.service.PaymentEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentEventHandler paymentEventHandler;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

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

    @PostMapping("/test/payment-success")
    public ResponseEntity<Void> testPaymentSuccess(@RequestBody PaymentSuccessEvent event) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PAYMENT_SUCCESS_TOPIC, jsonEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing PaymentSuccessEvent", e);
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/test/payment-failed")
    public ResponseEntity<Void> testPaymentFailed(@RequestBody PaymentFailedEvent event) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PAYMENT_FAILED_TOPIC, jsonEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing PaymentFailedEvent", e);
        }
        return ResponseEntity.accepted().build();
    }

}
