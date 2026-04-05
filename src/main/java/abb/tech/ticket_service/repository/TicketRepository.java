package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByOrderId(Long orderId);
    Optional<Ticket> findByTicketNumber(UUID ticketNumber);
    List<Ticket> findBySeatIdAndEventSessionId(Long seatId, Long eventSessionId);

    @EntityGraph(attributePaths = {
            "eventSession",
            "eventSession.event",
            "eventSession.hall",
            "eventSession.hall.venue",
            "seat",
            "seat.row",
            "seat.row.block",
            "seat.row.block.section"
    })
    Optional<Ticket> findWithDetailsById(Long id);
}
