package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallRepository extends JpaRepository<Hall, Long> {
}
