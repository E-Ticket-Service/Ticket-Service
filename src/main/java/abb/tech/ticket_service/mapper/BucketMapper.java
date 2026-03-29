package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.response.RespBucketItemDto;
import abb.tech.ticket_service.dto.response.RespBucketDto;
import abb.tech.ticket_service.model.Bucket;
import abb.tech.ticket_service.model.BucketItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BucketMapper {

    public RespBucketItemDto toItemResponse(BucketItem item) {
        return new RespBucketItemDto(
                item.getId(),
                item.getBucket() != null ? item.getBucket().getId() : null,
                item.getEventSession() != null ? item.getEventSession().getId() : null,
                item.getSeat() != null ? item.getSeat().getId() : null,
                item.isSelected(),
                item.getCount(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    public RespBucketDto toBucketResponse(Bucket bucket, List<BucketItem> items) {
        List<RespBucketItemDto> itemResponses = items == null
                ? Collections.emptyList()
                : items.stream().map(this::toItemResponse).toList();

        return new RespBucketDto(
                bucket.getId(),
                bucket.getUserId(),
                itemResponses,
                bucket.getCreatedAt(),
                bucket.getUpdatedAt()
        );
    }
}
