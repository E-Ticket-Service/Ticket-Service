package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
