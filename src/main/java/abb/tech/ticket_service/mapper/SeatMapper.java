package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.request.SeatCreationRequest;
import abb.tech.ticket_service.dto.request.SeatUpdateRequest;
import abb.tech.ticket_service.dto.response.SeatResponse;
import abb.tech.ticket_service.model.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    Seat toEntity(SeatCreationRequest request);
    SeatResponse toResponse(Seat seat);

    void updateSeat(@MappingTarget Seat seat, SeatUpdateRequest request);
}
