package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.request.RowCreationRequest;
import abb.tech.ticket_service.dto.request.RowUpdateRequest;
import abb.tech.ticket_service.dto.response.RowResponse;
import abb.tech.ticket_service.model.Row;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SeatMapper.class})
public interface RowMapper {

    Row toEntity(RowCreationRequest request);
    RowResponse toResponse(Row row);
    void updateRow(@MappingTarget Row row, RowUpdateRequest request);
}
