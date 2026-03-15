package abb.tech.ticket_service.dto.request;

import abb.tech.ticket_service.enums.EventCategory;
import abb.tech.ticket_service.enums.EventStatus;

import java.util.List;
import java.util.UUID;

public record ReqEventDto(
        String name, String description,
        Integer ageLimit,
        Double durationMinutes,
        String language,
        List<UUID> attachments,
        EventCategory category, EventStatus eventStatus
) {
}
