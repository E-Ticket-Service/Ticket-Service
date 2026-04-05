package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.ReqEventDto;
import abb.tech.ticket_service.dto.response.RespEventDto;
import abb.tech.ticket_service.service.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Controller", description = "Endpoints for managing corporate and social events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
//    @PreAuthorize("hasRole('ADMIN')")
    public RespEventDto createEvent(@Valid @RequestBody ReqEventDto reqEventDto) {
        return eventService.createEvent(reqEventDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
//    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public RespEventDto updateEvent(@Valid @RequestBody ReqEventDto reqEventDto, @Valid @PathVariable Long id) {
        return eventService.updateEvent(id, reqEventDto);
    }

    @GetMapping("/by-id")
    @ResponseStatus(HttpStatus.ACCEPTED)
//    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public RespEventDto getEventById(@RequestParam(value = "id") Long id) {
        return eventService.getEventById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
//    @PreAuthorize("hasRole('ADMIN')")
    public List<RespEventDto> getAllEvents() {
        return eventService.getAllEvents();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }


}
