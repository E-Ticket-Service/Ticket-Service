package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Row;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RowRepository extends JpaRepository<Row, Long> {
}
