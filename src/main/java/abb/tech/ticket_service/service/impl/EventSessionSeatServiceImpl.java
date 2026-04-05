package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.model.EventSessionSeat;
import abb.tech.ticket_service.repository.EventSessionSeatRepository;
import abb.tech.ticket_service.service.EventSessionSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventSessionSeatServiceImpl implements EventSessionSeatService {

    private final EventSessionSeatRepository eventSessionSeatRepository;

    @Override
    public EventSessionSeat findByEventSessionIdAndSeatId(Long eventSessionId, Long seatId) {
        return eventSessionSeatRepository.findByEventSessionIdAndSeatId(eventSessionId, seatId)
                .orElseThrow(() -> new IllegalStateException("Seat not found for this session"));
    }

    @Override
    @Transactional
    public void create(EventSessionSeat eventSessionSeat) {
        eventSessionSeatRepository.save(eventSessionSeat);
    }

    @Override
    @Transactional
    public void createAll(List<EventSessionSeat> eventSessionSeats) {
        eventSessionSeatRepository.saveAll(eventSessionSeats);
    }
}
