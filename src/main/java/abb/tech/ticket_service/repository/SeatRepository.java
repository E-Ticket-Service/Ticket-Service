package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
