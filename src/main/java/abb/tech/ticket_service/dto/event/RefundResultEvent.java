package abb.tech.ticket_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundResultEvent {
    private String refundId;
    private String paymentIntentId;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private LocalDateTime timestamp;
}
