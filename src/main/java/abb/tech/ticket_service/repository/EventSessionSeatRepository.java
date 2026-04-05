package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.EventSessionSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventSessionSeatRepository extends JpaRepository<EventSessionSeat, Long> {
    Optional<EventSessionSeat> findByEventSessionIdAndSeatId(Long eventSessionId, Long seatId);
}
