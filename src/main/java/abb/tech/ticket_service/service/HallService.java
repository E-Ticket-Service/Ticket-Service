package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.HallCreationRequest;
import abb.tech.ticket_service.dto.request.HallUpdateRequest;
import abb.tech.ticket_service.model.Hall;
import abb.tech.ticket_service.model.Venue;

import java.util.List;

public interface HallService {

    Hall getById(Long id);
    void createHall(HallCreationRequest request, Long venueId);
    void updateHall(Long id, HallUpdateRequest request);
    void deleteHall(Long id);
}
