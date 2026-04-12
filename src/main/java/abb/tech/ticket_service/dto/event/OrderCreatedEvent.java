package abb.tech.ticket_service.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String currency;
    private String description;
    private String paymentMethod;
}
