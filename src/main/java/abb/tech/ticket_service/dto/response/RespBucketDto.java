package abb.tech.ticket_service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RespBucketDto(
        Long id,
        Long userId,
        List<RespBucketItemDto> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
