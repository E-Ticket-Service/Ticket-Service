package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.ReqEventSessionDto;
import abb.tech.ticket_service.dto.response.RespEventSessionDto;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.exception.SessionTimeConflictException;
import abb.tech.ticket_service.mapper.EventSessionMapper;
import abb.tech.ticket_service.model.Event;
import abb.tech.ticket_service.model.EventSession;
import abb.tech.ticket_service.model.Hall;
import abb.tech.ticket_service.repository.EventRepository;
import abb.tech.ticket_service.repository.EventSessionRepository;
import abb.tech.ticket_service.repository.HallRepository;
import abb.tech.ticket_service.service.serviceImpl.EventSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSessionServiceImplTest {

    @Mock
    private EventSessionRepository eventSessionRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private EventSessionMapper mapper;

    @InjectMocks
    private EventSessionServiceImpl service;
    private Event event;
    private Hall hall;
    private EventSession session;
    private ReqEventSessionDto validRequest;
    private RespEventSessionDto sessionResponse;

    private static final Long EVENT_ID   = 1L;
    private static final Long HALL_ID    = 10L;
    private static final Long SESSION_ID = 100L;

    private static final LocalDateTime START = LocalDateTime.of(2025, 6, 1, 10, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2025, 6, 1, 12, 0);

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(EVENT_ID);
        event.setName("Test Event");

        hall = new Hall();
        hall.setId(HALL_ID);
        hall.setName("Main Hall");

        session = new EventSession();
        session.setId(SESSION_ID);
        session.setEvent(event);
        session.setHall(hall);
        session.setStartTime(START);
        session.setEndTime(END);
        session.setBasePrice(BigDecimal.valueOf(50));
        session.setAvailableSeats(100);

        validRequest = new ReqEventSessionDto(HALL_ID, START, END, BigDecimal.valueOf(50), 100);

        sessionResponse = new RespEventSessionDto(
                SESSION_ID, EVENT_ID, "Test Event",
                HALL_ID, "Main Hall",
                START, END,
                BigDecimal.valueOf(50), 100,
                null, null
        );
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Uğurlu əldəetmə — session qaytarılır")
        void getById_success() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
            when(mapper.toResponse(session)).thenReturn(sessionResponse);

            RespEventSessionDto result = service.getById(EVENT_ID, SESSION_ID);

            assertThat(result).isEqualTo(sessionResponse);
        }

        @Test
        @DisplayName("Event tapılmadıqda ResourceNotFoundException atılır")
        void getById_eventNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(EVENT_ID, SESSION_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Session tapılmadıqda ResourceNotFoundException atılır")
        void getById_sessionNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(EVENT_ID, SESSION_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Session başqa event-ə aiddirsə ResourceNotFoundException atılır")
        void getById_sessionBelongsToDifferentEvent() {
            Event otherEvent = new Event();
            otherEvent.setId(999L);
            session.setEvent(otherEvent);

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> service.getById(EVENT_ID, SESSION_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GET ALL BY EVENT
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllByEvent()")
    class GetAllByEventTests {

        @Test
        @DisplayName("Event-ə aid bütün sessionlar qaytarılır")
        void getAllByEvent_success() {
            EventSession session2 = new EventSession();
            session2.setId(101L);
            session2.setEvent(event);
            session2.setHall(hall);
            session2.setStartTime(END.plusHours(1));
            session2.setEndTime(END.plusHours(3));

            RespEventSessionDto response2 = new RespEventSessionDto(
                    101L, EVENT_ID, "Test Event", HALL_ID, "Main Hall",
                    END.plusHours(1), END.plusHours(3),
                    BigDecimal.valueOf(50), 100, null, null
            );

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findByEventId(EVENT_ID)).thenReturn(List.of(session, session2));
            when(mapper.toResponse(session)).thenReturn(sessionResponse);
            when(mapper.toResponse(session2)).thenReturn(response2);

            List<RespEventSessionDto> result = service.getAllByEvent(EVENT_ID);

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(sessionResponse, response2);
        }

        @Test
        @DisplayName("Event-ə aid session yoxdursa boş list qaytarılır")
        void getAllByEvent_emptyList() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findByEventId(EVENT_ID)).thenReturn(List.of());

            List<RespEventSessionDto> result = service.getAllByEvent(EVENT_ID);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Event tapılmadıqda ResourceNotFoundException atılır")
        void getAllByEvent_eventNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getAllByEvent(EVENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(eventSessionRepository, never()).findByEventId(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Uğurlu yaratma — bütün məlumatlar düzgündürsə session qaytarılır")
        void create_success() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));
            when(eventSessionRepository.findOverlappingSessions(HALL_ID, START, END, null))
                    .thenReturn(List.of());
            when(eventSessionRepository.save(any(EventSession.class))).thenReturn(session);
            when(mapper.toResponse(session)).thenReturn(sessionResponse);

            RespEventSessionDto result = service.create(EVENT_ID, validRequest);

            assertThat(result).isEqualTo(sessionResponse);
            verify(eventSessionRepository).save(any(EventSession.class));
        }

        @Test
        @DisplayName("Event tapılmadıqda ResourceNotFoundException atılır")
        void create_eventNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(EVENT_ID, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(EVENT_ID));

            verifyNoInteractions(eventSessionRepository);
        }

        @Test
        @DisplayName("Hall tapılmadıqda ResourceNotFoundException atılır")
        void create_hallNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(EVENT_ID, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(HALL_ID));
        }

        @Test
        @DisplayName("endTime <= startTime olduqda IllegalArgumentException atılır")
        void create_invalidTimeRange() {
            ReqEventSessionDto badRequest = new ReqEventSessionDto(
                    HALL_ID, END, START, BigDecimal.valueOf(50), 100); // reversed

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));

            assertThatThrownBy(() -> service.create(EVENT_ID, badRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("endTime");
        }

        @Test
        @DisplayName("Eyni Hall-da zaman kəsişməsi olduqda SessionTimeConflictException atılır")
        void create_overlapConflict() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));
            when(eventSessionRepository.findOverlappingSessions(HALL_ID, START, END, null))
                    .thenReturn(List.of(session));

            assertThatThrownBy(() -> service.create(EVENT_ID, validRequest))
                    .isInstanceOf(SessionTimeConflictException.class)
                    .hasMessageContaining(String.valueOf(HALL_ID));

            verify(eventSessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("startTime == endTime olduqda IllegalArgumentException atılır")
        void create_startEqualsEnd() {
            ReqEventSessionDto sameTime = new ReqEventSessionDto(
                    HALL_ID, START, START, BigDecimal.valueOf(50), 100);

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));

            assertThatThrownBy(() -> service.create(EVENT_ID, sameTime))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Uğurlu yeniləmə — dəyişdirilmiş session qaytarılır")
        void update_success() {
            ReqEventSessionDto updatedRequest = new ReqEventSessionDto(
                    HALL_ID,
                    START.plusHours(1),
                    END.plusHours(1),
                    BigDecimal.valueOf(75),
                    80
            );
            RespEventSessionDto updatedResponse = new RespEventSessionDto(
                    SESSION_ID, EVENT_ID, "Test Event",
                    HALL_ID, "Main Hall",
                    START.plusHours(1), END.plusHours(1),
                    BigDecimal.valueOf(75), 80,
                    null, null
            );

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));
            when(eventSessionRepository.findOverlappingSessions(
                    HALL_ID, START.plusHours(1), END.plusHours(1), SESSION_ID))
                    .thenReturn(List.of());
            when(eventSessionRepository.save(session)).thenReturn(session);
            when(mapper.toResponse(session)).thenReturn(updatedResponse);

            RespEventSessionDto result = service.update(EVENT_ID, SESSION_ID, updatedRequest);

            assertThat(result.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(75));
            assertThat(result.availableSeats()).isEqualTo(80);
        }

        @Test
        @DisplayName("Update zamanı özü ilə kəsişmə yoxlanmır (excludeId = sessionId)")
        void update_doesNotConflictWithItself() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));
            when(eventSessionRepository.findOverlappingSessions(HALL_ID, START, END, SESSION_ID))
                    .thenReturn(List.of()); // özünü exclude etdi, nəticə boş
            when(eventSessionRepository.save(session)).thenReturn(session);
            when(mapper.toResponse(session)).thenReturn(sessionResponse);

            // Bu call exception atmadan tamamlanmalıdır
            service.update(EVENT_ID, SESSION_ID, validRequest);

            verify(eventSessionRepository).findOverlappingSessions(HALL_ID, START, END, SESSION_ID);
        }

        @Test
        @DisplayName("Session başqa event-ə aiddirsə ResourceNotFoundException atılır")
        void update_sessionBelongsToDifferentEvent() {
            Event otherEvent = new Event();
            otherEvent.setId(999L);
            session.setEvent(otherEvent);

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> service.update(EVENT_ID, SESSION_ID, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(SESSION_ID));
        }

        @Test
        @DisplayName("Fərqli bir session ilə kəsişmə varsa SessionTimeConflictException atılır")
        void update_conflictWithAnotherSession() {
            EventSession conflictingSession = new EventSession();
            conflictingSession.setId(200L);
            conflictingSession.setStartTime(START);
            conflictingSession.setEndTime(END);

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));
            when(eventSessionRepository.findOverlappingSessions(HALL_ID, START, END, SESSION_ID))
                    .thenReturn(List.of(conflictingSession));

            assertThatThrownBy(() -> service.update(EVENT_ID, SESSION_ID, validRequest))
                    .isInstanceOf(SessionTimeConflictException.class);

            verify(eventSessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Session tapılmadıqda ResourceNotFoundException atılır")
        void update_sessionNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(EVENT_ID, SESSION_ID, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(SESSION_ID));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Uğurlu silmə — delete çağırılır")
        void delete_success() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

            service.delete(EVENT_ID, SESSION_ID);

            verify(eventSessionRepository).delete(session);
        }

        @Test
        @DisplayName("Event tapılmadıqda ResourceNotFoundException atılır")
        void delete_eventNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(EVENT_ID, SESSION_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(eventSessionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Session tapılmadıqda ResourceNotFoundException atılır")
        void delete_sessionNotFound() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(EVENT_ID, SESSION_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(eventSessionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Session başqa event-ə aiddirsə ResourceNotFoundException atılır, silmə baş vermir")
        void delete_sessionBelongsToDifferentEvent() {
            Event otherEvent = new Event();
            otherEvent.setId(999L);
            session.setEvent(otherEvent);

            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> service.delete(EVENT_ID, SESSION_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(eventSessionRepository, never()).delete(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // OVERLAP EDGE CASES
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Zaman kəsişməsi — edge case-lər")
    class OverlapEdgeCaseTests {

        private void stubCreatePrerequisites() {
            when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));
        }

        @Test
        @DisplayName("Yeni session mövcud session-dan əvvəl bitdikdə kəsişmə yoxdur")
        void noOverlap_newSessionEndsBefore() {
            // existing: 12:00-14:00, new: 10:00-12:00 (touching at boundary — no overlap)
            ReqEventSessionDto req = new ReqEventSessionDto(
                    HALL_ID,
                    LocalDateTime.of(2025, 6, 1, 10, 0),
                    LocalDateTime.of(2025, 6, 1, 12, 0),
                    BigDecimal.valueOf(50), 100
            );
            stubCreatePrerequisites();
            when(eventSessionRepository.findOverlappingSessions(
                    eq(HALL_ID), any(), any(), eq(null)))
                    .thenReturn(List.of());
            when(eventSessionRepository.save(any())).thenReturn(session);
            when(mapper.toResponse(any())).thenReturn(sessionResponse);

            // should not throw
            service.create(EVENT_ID, req);
        }

        @Test
        @DisplayName("Yeni session mövcud session-dan sonra başladıqda kəsişmə yoxdur")
        void noOverlap_newSessionStartsAfter() {
            ReqEventSessionDto req = new ReqEventSessionDto(
                    HALL_ID,
                    LocalDateTime.of(2025, 6, 1, 14, 0),
                    LocalDateTime.of(2025, 6, 1, 16, 0),
                    BigDecimal.valueOf(50), 100
            );
            stubCreatePrerequisites();
            when(eventSessionRepository.findOverlappingSessions(
                    eq(HALL_ID), any(), any(), eq(null)))
                    .thenReturn(List.of());
            when(eventSessionRepository.save(any())).thenReturn(session);
            when(mapper.toResponse(any())).thenReturn(sessionResponse);

            service.create(EVENT_ID, req);
            verify(eventSessionRepository).save(any());
        }

        @Test
        @DisplayName("Yeni session mövcud session-u tam əhatə etdikdə kəsişmə var")
        void overlap_newSessionCoversExisting() {
            EventSession existing = new EventSession();
            existing.setId(200L);
            existing.setStartTime(LocalDateTime.of(2025, 6, 1, 11, 0));
            existing.setEndTime(LocalDateTime.of(2025, 6, 1, 13, 0));

            ReqEventSessionDto req = new ReqEventSessionDto(
                    HALL_ID,
                    LocalDateTime.of(2025, 6, 1, 10, 0),
                    LocalDateTime.of(2025, 6, 1, 14, 0),
                    BigDecimal.valueOf(50), 100
            );
            stubCreatePrerequisites();
            when(eventSessionRepository.findOverlappingSessions(
                    eq(HALL_ID), any(), any(), eq(null)))
                    .thenReturn(List.of(existing));

            assertThatThrownBy(() -> service.create(EVENT_ID, req))
                    .isInstanceOf(SessionTimeConflictException.class);
        }

        @Test
        @DisplayName("Mövcud session yeni session-u tam əhatə etdikdə kəsişmə var")
        void overlap_existingCoversNew() {
            EventSession existing = new EventSession();
            existing.setId(200L);
            existing.setStartTime(LocalDateTime.of(2025, 6, 1, 9, 0));
            existing.setEndTime(LocalDateTime.of(2025, 6, 1, 15, 0));

            ReqEventSessionDto req = new ReqEventSessionDto(
                    HALL_ID,
                    LocalDateTime.of(2025, 6, 1, 10, 0),
                    LocalDateTime.of(2025, 6, 1, 12, 0),
                    BigDecimal.valueOf(50), 100
            );
            stubCreatePrerequisites();
            when(eventSessionRepository.findOverlappingSessions(
                    eq(HALL_ID), any(), any(), eq(null)))
                    .thenReturn(List.of(existing));

            assertThatThrownBy(() -> service.create(EVENT_ID, req))
                    .isInstanceOf(SessionTimeConflictException.class);
        }
    }
}
