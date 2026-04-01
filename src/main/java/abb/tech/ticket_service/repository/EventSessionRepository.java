package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventSessionRepository extends JpaRepository<EventSession, Long> {

    List<EventSession> findByEventId(Long eventId);

    /**
     * Eyni Hall-da verilmiş zaman aralığı ilə kəsişən EventSession-ları tapır.
     * excludeId parametri update zamanı özünü nəzərə almamaq üçün istifadə olunur.
     *
     * Kəsişmə şərti: startA < endB AND endA > startB
     */
    @Query("""
            SELECT es FROM EventSession es
            WHERE es.hall.id = :hallId
              AND (:excludeId IS NULL OR es.id <> :excludeId)
              AND es.startTime < :endTime
              AND es.endTime > :startTime
            """)
    List<EventSession> findOverlappingSessions(
            @Param("hallId") Long hallId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId
    );
}
