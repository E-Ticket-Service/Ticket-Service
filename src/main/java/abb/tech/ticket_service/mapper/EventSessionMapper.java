package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.response.RespEventSessionDto;
import abb.tech.ticket_service.model.EventSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventSessionMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "hallId", source = "hall.id")
    @Mapping(target = "hallName", source = "hall.name")
    RespEventSessionDto toResponse(EventSession session);
}
