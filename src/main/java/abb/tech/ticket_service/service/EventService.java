package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.ReqEventDto;
import abb.tech.ticket_service.dto.response.RespEventDto;

import java.util.List;

public interface EventService {
    RespEventDto createEvent(ReqEventDto reqEventDto);
    RespEventDto updateEvent(Long id,ReqEventDto reqEventDto);
    RespEventDto getEventById(Long id);
    List<RespEventDto> getAllEvents();
    void deleteEvent(Long id);

}
