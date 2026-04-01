package abb.tech.ticket_service.dto.response;

import java.time.LocalDateTime;

public record RespBucketItemDto(
        Long id,
        Long bucketId,
        Long eventSessionId,
        Long seatId,
        boolean selected,
        int count,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
