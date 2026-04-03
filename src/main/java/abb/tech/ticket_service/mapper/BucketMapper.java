package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.response.RespBucketDto;
import abb.tech.ticket_service.dto.response.RespBucketItemDto;
import abb.tech.ticket_service.model.Bucket;
import abb.tech.ticket_service.model.BucketItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BucketMapper {

    @Mapping(target = "bucketId", source = "bucket.id")
    @Mapping(target = "eventSessionId", source = "eventSession.id")
    @Mapping(target = "seatId", source = "seat.id")
    RespBucketItemDto toItemResponse(BucketItem item);

    @Mapping(target = "items", source = "items")
    RespBucketDto toBucketResponse(Bucket bucket, List<BucketItem> items);
}
