package abb.tech.ticket_service.dto.response;

import abb.tech.ticket_service.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    Long userId;
    BigDecimal totalAmount;
    OrderStatus orderStatus;
    List<OrderItemResponse> orderItems;
}
