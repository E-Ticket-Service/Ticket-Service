package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.request.ReqEventDto;
import abb.tech.ticket_service.dto.response.RespEventDto;
import abb.tech.ticket_service.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toEntity(ReqEventDto eventRequest);
    RespEventDto toResponse(Event event);
    void updateEntityFromDto(ReqEventDto reqEventDto, @MappingTarget Event event);
    List<RespEventDto>toDoList(List<Event> events);
}
