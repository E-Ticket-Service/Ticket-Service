package abb.tech.ticket_service.dto.request;

import abb.tech.ticket_service.enums.EventCategory;
import abb.tech.ticket_service.enums.EventStatus;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record ReqEventDto(
        @NotBlank(message = "name can not be empty or null")
        String name,
        @Size(min = 10,max = 500,message ="description must be between 10 and 500 character" )
        String description,
        @NotNull(message = "age cannot be null")
        @Positive(message = "age can't be negative")
        Integer ageLimit,
        @NotNull(message = "duration is required")
        @Positive(message = "duration must be a positive number")
        Double durationMinutes,
        String language,
        @NotEmpty(message = "at least one attachment is required")
        List<UUID> attachments,
        @NotNull(message = "category cannot be null")
        EventCategory category,
        @NotNull(message = "status cannot be null")
        EventStatus eventStatus
) {
}
