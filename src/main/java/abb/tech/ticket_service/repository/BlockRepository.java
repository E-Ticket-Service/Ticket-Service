package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {
}
