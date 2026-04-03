package abb.tech.ticket_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemCreationRequest {

    @NotNull(message = "Order ID cannot be null")
    Long eventSessionId;
    @NotNull(message = "Seat ID cannot be null")
    Long seatId;
}
