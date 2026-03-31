package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.HallCreationRequest;
import abb.tech.ticket_service.dto.request.HallUpdateRequest;
import abb.tech.ticket_service.service.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @PutMapping("/venue/{venueId}")
    public void createHalls(@Valid @RequestBody HallCreationRequest request, @PathVariable Long venueId){
        hallService.createHall(request, venueId);
    }

    @PutMapping("/{id}")
    public void updateHall(@PathVariable Long id, @RequestBody HallUpdateRequest request){
        hallService.updateHall(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteHall(@PathVariable Long id){
        hallService.deleteHall(id);
    }
}
