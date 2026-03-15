package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.ReqEventDto;
import abb.tech.ticket_service.dto.response.RespEventDto;
import abb.tech.ticket_service.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)

    public RespEventDto createEvent(@Valid @RequestBody ReqEventDto reqEventDto) {
        return eventService.createEvent(reqEventDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RespEventDto updateEvent(@Valid @RequestBody ReqEventDto reqEventDto, @Valid @PathVariable Long id) {
        return eventService.updateEvent(id, reqEventDto);
    }

    @GetMapping("/by-id")
    @ResponseStatus(HttpStatus.ACCEPTED)

    public RespEventDto getEventById(@RequestParam(value = "id") Long id) {
        return eventService.getEventById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<RespEventDto> getAllEvents() {
        return eventService.getAllEvents();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }


}
