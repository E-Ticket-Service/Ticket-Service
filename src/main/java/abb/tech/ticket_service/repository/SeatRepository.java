package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.row.block.section.hall.id = :hallId")
    List<Seat> findAllByHallId(@Param("hallId") Long hallId);
}
