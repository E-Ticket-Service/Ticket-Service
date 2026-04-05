package abb.tech.ticket_service.service;

import abb.tech.ticket_service.model.EventSessionSeat;
import java.util.List;

public interface EventSessionSeatService {
    EventSessionSeat findByEventSessionIdAndSeatId(Long eventSessionId, Long seatId);
    void create(EventSessionSeat eventSessionSeat);
    void createAll(List<EventSessionSeat> eventSessionSeats);
}
