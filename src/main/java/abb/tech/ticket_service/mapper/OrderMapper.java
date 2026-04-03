package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.request.OrderCreationRequest;
import abb.tech.ticket_service.dto.response.OrderResponse;
import abb.tech.ticket_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {
    OrderResponse toResponse(Order order);

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "totalAmount", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "orderStatus", constant = "IN_PROGRESS")
    Order toEntity(OrderCreationRequest request);
}
