package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.VenueCreationRequest;
import abb.tech.ticket_service.dto.request.VenueUpdateRequest;
import abb.tech.ticket_service.dto.response.VenueResponse;

public interface VenueService {

    VenueResponse getVenueById(Long id);
    void createVenue(VenueCreationRequest request);
    void updateVenue(Long id, VenueUpdateRequest request);
}
