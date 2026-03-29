package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.response.RespEventSessionDto;
import abb.tech.ticket_service.model.EventSession;
import org.springframework.stereotype.Component;

@Component
public class EventSessionMapper {

    public RespEventSessionDto toResponse(EventSession session) {
        return new RespEventSessionDto(
                session.getId(),
                session.getEvent() != null ? session.getEvent().getId() : null,
                session.getEvent() != null ? session.getEvent().getName() : null,
                session.getHall() != null ? session.getHall().getId() : null,
                session.getHall() != null ? session.getHall().getName() : null,
                session.getStartTime(),
                session.getEndTime(),
                session.getBasePrice(),
                session.getAvailableSeats(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
