package abb.tech.ticket_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundRequestEvent {
    private String paymentIntentId;
    private BigDecimal amount;
    private Long orderId;
}
