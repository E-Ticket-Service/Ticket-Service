package abb.tech.ticket_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RespEventSessionDto(
        Long id,
        Long eventId,
        String eventName,
        Long hallId,
        String hallName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal basePrice,
        Integer availableSeats,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
