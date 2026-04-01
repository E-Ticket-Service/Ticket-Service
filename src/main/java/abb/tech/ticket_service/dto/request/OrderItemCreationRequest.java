package abb.tech.ticket_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemCreationRequest {

    @NotNull(message = "Order ID cannot be null")
    Long eventSessionId;
    @NotNull(message = "Seat ID cannot be null")
    Long seatId;
}
