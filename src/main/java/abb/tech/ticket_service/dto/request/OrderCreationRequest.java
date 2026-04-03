package abb.tech.ticket_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationRequest {

    @NotNull(message = "User ID cannot be null")
    Long userId;

    @Valid
    List<OrderItemCreationRequest> orderItems;
}
