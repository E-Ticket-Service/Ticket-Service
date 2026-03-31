package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.request.VenueCreationRequest;
import abb.tech.ticket_service.dto.request.VenueUpdateRequest;
import abb.tech.ticket_service.dto.response.VenueResponse;
import abb.tech.ticket_service.model.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {HallMapper.class})
public interface VenueMapper {

    Venue toEntity(VenueCreationRequest request);
    VenueResponse toResponse(Venue venue);
    void updateVenue(@MappingTarget Venue venue, VenueUpdateRequest request);
}
