package abb.tech.ticket_service.mapper;

import abb.tech.ticket_service.dto.request.SectionCreationRequest;
import abb.tech.ticket_service.dto.request.SectionUpdateRequest;
import abb.tech.ticket_service.dto.response.SectionResponse;
import abb.tech.ticket_service.model.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {BlockMapper.class})
public interface SectionMapper {

    Section toEntity(SectionCreationRequest request);
    SectionResponse toResponse(Section section);
    void updateSection(@MappingTarget Section section, SectionUpdateRequest request);
}
