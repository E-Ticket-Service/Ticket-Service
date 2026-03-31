package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.VenueCreationRequest;
import abb.tech.ticket_service.dto.request.VenueUpdateRequest;
import abb.tech.ticket_service.dto.response.VenueResponse;
import abb.tech.ticket_service.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping("/{id}")
    public VenueResponse createVenue(@PathVariable Long id){
        return venueService.getVenueById(id);
    }

    @PostMapping
    public void createVenue(@RequestBody VenueCreationRequest request){
        venueService.createVenue(request);
    }

    @PutMapping("/{id}")
    public void updateVenue(@PathVariable Long id, @RequestBody VenueUpdateRequest request){
        venueService.updateVenue(id, request);
    }

}
