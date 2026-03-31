package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.request.HallCreationRequest;
import abb.tech.ticket_service.dto.request.HallUpdateRequest;
import abb.tech.ticket_service.dto.response.HallResponse;
import abb.tech.ticket_service.model.Hall;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SectionMapper.class})
public interface HallMapper {

//    @Mapping(target = "sections", ignore = true)
    Hall toEntity(HallCreationRequest request);
    HallResponse toResponse(Hall hall);
    void updateHall(@MappingTarget Hall hall, HallUpdateRequest request);
}
