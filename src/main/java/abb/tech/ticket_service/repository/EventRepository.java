package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event,Long> {

    Boolean existsByName(String name);
}
