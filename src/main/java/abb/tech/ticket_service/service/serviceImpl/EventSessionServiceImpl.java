package abb.tech.ticket_service.service.serviceImpl;

import abb.tech.ticket_service.dto.request.ReqEventSessionDto;
import abb.tech.ticket_service.dto.response.RespEventSessionDto;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.exception.SessionTimeConflictException;
import abb.tech.ticket_service.mapper.EventSessionMapper;
import abb.tech.ticket_service.model.Event;
import abb.tech.ticket_service.model.EventSession;
import abb.tech.ticket_service.model.Hall;
import abb.tech.ticket_service.repository.EventRepository;
import abb.tech.ticket_service.repository.EventSessionRepository;
import abb.tech.ticket_service.repository.HallRepository;
import abb.tech.ticket_service.service.EventSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventSessionServiceImpl implements EventSessionService {

    private final EventSessionRepository eventSessionRepository;
    private final EventRepository eventRepository;
    private final HallRepository hallRepository;
    private final EventSessionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public EventSession findById(Long id) {
        return eventSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventSession tapılmadı, id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public RespEventSessionDto getById(Long eventId, Long sessionId) {
        findEventOrThrow(eventId);
        EventSession session = findSessionOrThrow(sessionId, eventId);
        return mapper.toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespEventSessionDto> getAllByEvent(Long eventId) {
        findEventOrThrow(eventId);
        return eventSessionRepository.findByEventId(eventId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RespEventSessionDto create(Long eventId, ReqEventSessionDto request) {
        Event event = findEventOrThrow(eventId);
        Hall hall = findHallOrThrow(request.hallId());

        validateTimeRange(request);
        checkOverlap(request.hallId(), request.startTime(), request.endTime(), null);

        EventSession session = new EventSession();
        session.setEvent(event);
        session.setHall(hall);
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setBasePrice(request.basePrice());
        session.setAvailableSeats(request.availableSeats());

        return mapper.toResponse(eventSessionRepository.save(session));
    }

    @Override
    @Transactional
    public RespEventSessionDto update(Long eventId, Long sessionId, ReqEventSessionDto request) {
        findEventOrThrow(eventId);
        EventSession session = findSessionOrThrow(sessionId, eventId);
        Hall hall = findHallOrThrow(request.hallId());

        validateTimeRange(request);
        checkOverlap(request.hallId(), request.startTime(), request.endTime(), sessionId);

        session.setHall(hall);
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setBasePrice(request.basePrice());
        session.setAvailableSeats(request.availableSeats());

        return mapper.toResponse(eventSessionRepository.save(session));
    }

    @Override
    @Transactional
    public void delete(Long eventId, Long sessionId) {
        findEventOrThrow(eventId);
        EventSession session = findSessionOrThrow(sessionId, eventId);
        eventSessionRepository.delete(session);
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event tapılmadı, id: " + eventId));
    }

    private Hall findHallOrThrow(Long hallId) {
        return hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hall tapılmadı, id: " + hallId));
    }

    private EventSession findSessionOrThrow(Long sessionId, Long eventId) {
        EventSession session = eventSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EventSession tapılmadı, id: " + sessionId));

        if (!session.getEvent().getId().equals(eventId)) {
            throw new ResourceNotFoundException(
                    "EventSession id=" + sessionId + " bu event-ə aid deyil, eventId: " + eventId);
        }
        return session;
    }

    private void validateTimeRange(ReqEventSessionDto request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException(
                    "endTime startTime-dan sonra olmalıdır.");
        }
    }

    /**
     * Eyni Hall-da olan digər EventSession-larla zaman kəsişməsini yoxlayır.
     *
     * @param excludeId update əməliyyatında mövcud session-un id-si (null → create)
     */
    private void checkOverlap(Long hallId,
                               java.time.LocalDateTime startTime,
                               java.time.LocalDateTime endTime,
                               Long excludeId) {
        List<EventSession> conflicts = eventSessionRepository
                .findOverlappingSessions(hallId, startTime, endTime, excludeId);

        if (!conflicts.isEmpty()) {
            EventSession conflict = conflicts.getFirst();
            throw new SessionTimeConflictException(
                    "Seçilmiş Hall-da (id=%d) bu zaman aralığı (startTime=%s, endTime=%s) artıq mövcud sessiya ilə kəsişir. "
                    .formatted(hallId, startTime, endTime)
                    + "Kəsişən sessiya: id=%d, startTime=%s, endTime=%s"
                    .formatted(conflict.getId(), conflict.getStartTime(), conflict.getEndTime())
            );
        }
    }
}
