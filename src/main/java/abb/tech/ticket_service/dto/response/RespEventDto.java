package abb.tech.ticket_service.dto.response;

import abb.tech.ticket_service.enums.EventCategory;
import abb.tech.ticket_service.enums.EventStatus;
import abb.tech.ticket_service.model.Event;


import java.util.List;
import java.util.UUID;

public record RespEventDto(
        Long id,
        String name,
        String description,
        Integer ageLimit,
        Double durationMinutes,
        String language,
        List<UUID> attachments,
        EventCategory category,
        EventStatus eventStatus
) {


}
