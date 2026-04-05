package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.response.OrderItemResponse;
import abb.tech.ticket_service.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "eventSessionId", source = "eventSession.id")
    @Mapping(target = "seatId", source = "seat.id")
    OrderItemResponse toResponse(OrderItem orderItem);
}
