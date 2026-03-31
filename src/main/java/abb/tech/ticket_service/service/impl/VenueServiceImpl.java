package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.VenueCreationRequest;
import abb.tech.ticket_service.dto.request.VenueUpdateRequest;
import abb.tech.ticket_service.dto.response.VenueResponse;
import abb.tech.ticket_service.mapper.VenueMapper;
import abb.tech.ticket_service.model.Venue;
import abb.tech.ticket_service.repository.VenueRepository;
import abb.tech.ticket_service.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    @Override
    public Venue getById(Long id){
        return venueRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Venue not found."));
    }

    @Override
    @Transactional
    public VenueResponse getVenueById(Long id) {
        var venue = getById(id);
        return venueMapper.toResponse(venue);
    }

    @Override
    @Transactional
    public void createVenue(VenueCreationRequest request){
        var venue = venueMapper.toEntity(request);
        venueRepository.save(venue);
    }

    @Override
    @Transactional
    public void updateVenue(Long id, VenueUpdateRequest request) {
        var venue = getById(id);
        venueMapper.updateVenue(venue, request);
        venueRepository.save(venue);
    }

    @Override
    public void deleteVenue(Long id){
        var venue = getById(id);
        venueRepository.delete(venue);
    }

}
