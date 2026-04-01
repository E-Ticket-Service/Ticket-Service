package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
}
