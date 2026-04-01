package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.ReqEventSessionDto;
import abb.tech.ticket_service.dto.response.RespEventSessionDto;

import java.util.List;

public interface EventSessionService {

    RespEventSessionDto getById(Long eventId, Long sessionId);

    List<RespEventSessionDto> getAllByEvent(Long eventId);

    RespEventSessionDto create(Long eventId, ReqEventSessionDto request);

    RespEventSessionDto update(Long eventId, Long sessionId, ReqEventSessionDto request);

    void delete(Long eventId, Long sessionId);
}
