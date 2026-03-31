package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.SectionCreationRequest;
import abb.tech.ticket_service.dto.request.SectionUpdateRequest;
import abb.tech.ticket_service.model.Hall;

import java.util.List;

public interface SectionService {

    void createSections(List<SectionCreationRequest> requests, Hall hall);
    void updateSection(Long id, SectionUpdateRequest request);
    void deleteSection(Long id);
}
