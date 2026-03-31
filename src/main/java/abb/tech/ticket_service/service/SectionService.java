package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.SectionUpdateRequest;

public interface SectionService {

    void updateSection(Long id, SectionUpdateRequest request);
    void deleteSection(Long id);
}
