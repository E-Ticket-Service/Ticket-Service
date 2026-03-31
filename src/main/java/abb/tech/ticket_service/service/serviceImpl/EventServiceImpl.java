package abb.tech.ticket_service.service.serviceImpl;

import abb.tech.ticket_service.dto.request.ReqEventDto;
import abb.tech.ticket_service.dto.response.RespEventDto;
import abb.tech.ticket_service.exception.DuplicateEventException;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.mapper.EventMapper;
import abb.tech.ticket_service.model.Event;
import abb.tech.ticket_service.repository.EventRepository;
import abb.tech.ticket_service.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;


    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public RespEventDto createEvent(ReqEventDto reqEventDto) {
        if (eventRepository.existsByName(reqEventDto.name())) {
            throw new DuplicateEventException("An event with this name already exists:" + " " + reqEventDto.name());
        }
        Event event = eventMapper.toEntity(reqEventDto);
        eventRepository.save(event);
        log.info("Event successfully created with ID:" + event.getId() + " and " + "name: {" + event.getName() + "}");
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public RespEventDto updateEvent(Long id, ReqEventDto reqEventDto) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Event not found with ID:" + " " + id));
        eventMapper.updateEntityFromDto(reqEventDto, event);
        eventRepository.save(event);
        log.info("Update complete successfully ID:" + id);
        return eventMapper.toResponse(event);


    }

    @Override
    @Transactional(readOnly = true)
    public RespEventDto getEventById(@PathVariable Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Event not found with ID: " + " " + id));
        log.info("Event successfully found ID" + " " + id);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespEventDto> getAllEvents() {
        var events = eventRepository.findAll();
        log.info("All events were found successfully length:" + " " + events.toArray().length);
        return eventMapper.toDoList(events);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Event not found with ID:" + " " + id));
        eventRepository.deleteById(id);
        log.info("Event successfully deleted ID:" + " " + id);
    }

}
