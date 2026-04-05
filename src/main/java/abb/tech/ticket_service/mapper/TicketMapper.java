package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.response.TicketResponse;
import abb.tech.ticket_service.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "eventSessionId", source = "eventSession.id")
    @Mapping(target = "seatId", source = "seat.id")
    TicketResponse toResponse(Ticket ticket);
}
