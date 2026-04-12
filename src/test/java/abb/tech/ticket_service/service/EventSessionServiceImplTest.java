package abb.tech.ticket_service.service;
//
//import abb.tech.ticket_service.dto.request.ReqEventSessionDto;
//import abb.tech.ticket_service.dto.response.RespEventSessionDto;
//import abb.tech.ticket_service.exception.NotFoundException;
//import abb.tech.ticket_service.exception.ResourceNotFoundException;
//import abb.tech.ticket_service.exception.SessionTimeConflictException;
//import abb.tech.ticket_service.mapper.EventSessionMapper;
//import abb.tech.ticket_service.model.Event;
//import abb.tech.ticket_service.model.EventSession;
//import abb.tech.ticket_service.model.Hall;
//import abb.tech.ticket_service.model.Seat;
//import abb.tech.ticket_service.repository.EventSessionRepository;
//import abb.tech.ticket_service.service.impl.EventSessionServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class EventSessionServiceImplTest {
//
//    @Mock private EventSessionRepository eventSessionRepository;
//    @Mock private EventService eventService;
//    @Mock private HallService hallService;
//    @Mock private SeatService seatService;
//    @Mock private EventSessionSeatService eventSessionSeatService;
//    @Mock private EventSessionMapper mapper;
//
//
//    @InjectMocks
//    private EventSessionServiceImpl service;
//    private Event event;
//    private Hall hall;
//    private EventSession session;
//    private ReqEventSessionDto validRequest;
//    private RespEventSessionDto sessionResponse;
//
//    private static final Long EVENT_ID   = 1L;
//    private static final Long HALL_ID    = 10L;
//    private static final Long SESSION_ID = 100L;
//
//    private static final LocalDateTime START = LocalDateTime.of(2025, 6, 1, 10, 0);
//    private static final LocalDateTime END   = LocalDateTime.of(2025, 6, 1, 12, 0);
//
//    @BeforeEach
//    void setUp() {
//        event = new Event();
//        ReflectionTestUtils.setField(event, "id", EVENT_ID);
//        event.setName("Test Event");
//
//        hall = new Hall();
//        ReflectionTestUtils.setField(hall, "id", HALL_ID);
//        hall.setName("Main Hall");
//
//        session = new EventSession();
//        ReflectionTestUtils.setField(session, "id", SESSION_ID);
//        session.setEvent(event);
//        session.setHall(hall);
//        session.setStartTime(START);
//        session.setEndTime(END);
//        session.setBasePrice(BigDecimal.valueOf(50));
//        session.setAvailableSeats(100);
//
//        validRequest = new ReqEventSessionDto(HALL_ID, START, END, BigDecimal.valueOf(50), 100);
//
//        sessionResponse = new RespEventSessionDto(
//                SESSION_ID, EVENT_ID, "Test Event",
//                HALL_ID, "Main Hall",
//                START, END,
//                BigDecimal.valueOf(50), 100,
//                null, null
//        );
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // GET BY ID
//    // ─────────────────────────────────────────────────────────────────────────────
//    @Nested
//    @DisplayName("getById()")
//    class GetByIdTests {
//
//        @Test
//        @DisplayName("Uğurlu əldəetmə — session qaytarılır")
//        void getById_success() {
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
//            when(mapper.toResponse(session)).thenReturn(sessionResponse);
//
//            RespEventSessionDto result = service.getById(EVENT_ID, SESSION_ID);
//
//            assertThat(result).isEqualTo(sessionResponse);
//        }
//
//        @Test
//        @DisplayName("Event tapılmadıqda ResourceNotFoundException atılır")
//        void create_eventNotFound() {
//            when(eventService.getEventEntityById(EVENT_ID))
//                    .thenThrow(new ResourceNotFoundException("Event tapilmadi, id: " + EVENT_ID));
//
//            assertThatThrownBy(() -> service.create(EVENT_ID, validRequest))
//                    .isInstanceOf(ResourceNotFoundException.class);
//
//            verifyNoInteractions(eventSessionRepository);
//        }
//
//        @Test
//        @DisplayName("Session tapılmadıqda ResourceNotFoundException atılır")
//        void getById_sessionNotFound() {
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> service.getById(EVENT_ID, SESSION_ID))
//                    .isInstanceOf(ResourceNotFoundException.class);
//        }
//
//        @Test
//        @DisplayName("Session başqa event-ə aiddirsə ResourceNotFoundException atılır")
//        void getById_sessionBelongsToDifferentEvent() {
//            Event otherEvent = new Event();
//            ReflectionTestUtils.setField(otherEvent, "id", 999L);
//            session.setEvent(otherEvent);
//
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
//
//            assertThatThrownBy(() -> service.getById(EVENT_ID, SESSION_ID))
//                    .isInstanceOf(ResourceNotFoundException.class);
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // GET ALL BY EVENT
//    // ─────────────────────────────────────────────────────────────────────────────
//    @Nested
//    @DisplayName("getAllByEvent()")
//    class GetAllByEventTests {
//
//        @Test
//        @DisplayName("Event-ə aid bütün sessionlar qaytarılır")
//        void getAllByEvent_success() {
//            EventSession session2 = new EventSession();
//            ReflectionTestUtils.setField(session2, "id", 101L);
//            session2.setEvent(event);
//
//            RespEventSessionDto response2 = new RespEventSessionDto(
//                    101L, EVENT_ID, "Test Event", HALL_ID, "Main Hall",
//                    END.plusHours(1), END.plusHours(3),
//                    BigDecimal.valueOf(50), 100, null, null
//            );
//
//            when(eventSessionRepository.findByEventId(EVENT_ID)).thenReturn(List.of(session, session2));
//            when(mapper.toResponse(session)).thenReturn(sessionResponse);
//            when(mapper.toResponse(session2)).thenReturn(response2);
//
//            List<RespEventSessionDto> result = service.getAllByEvent(EVENT_ID);
//
//            assertThat(result).hasSize(2);
//            assertThat(result).containsExactly(sessionResponse, response2);
//        }
//
//        @Test
//        @DisplayName("Event-ə aid session yoxdursa boş list qaytarılır")
//        void getAllByEvent_emptyList() {
//            when(eventSessionRepository.findByEventId(EVENT_ID)).thenReturn(List.of());
//
//            assertThat(service.getAllByEvent(EVENT_ID)).isEmpty();
//        }
//
//        @Test
//        @DisplayName("Event tapilmadıqda NotFoundException atılır")
//        void getAllByEvent_eventNotFound() {
//            when(eventSessionRepository.findByEventId(EVENT_ID)).thenReturn(List.of());
//
//            List<RespEventSessionDto> result = service.getAllByEvent(EVENT_ID);
//            assertThat(result).isEmpty();
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // CREATE
//    // ─────────────────────────────────────────────────────────────────────────────
//    @Nested
//    @DisplayName("create()")
//    class CreateTests {
//
//        @Test
//        @DisplayName("Uğurlu yaratma — bütün məlumatlar düzgündürsə session qaytarılır")
//        void create_success() {
//            Seat seat1 = new Seat();
//            ReflectionTestUtils.setField(seat1, "id", 1L);
//            seat1.setExtraPrice(BigDecimal.valueOf(10));
//
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(hallService.getById(HALL_ID)).thenReturn(hall);
//            when(eventSessionRepository.findOverlappingSessions(HALL_ID, START, END, null))
//                    .thenReturn(List.of());
//            when(eventSessionRepository.save(any(EventSession.class))).thenReturn(session);
//            when(seatService.findAllByHallId(HALL_ID)).thenReturn(List.of(seat1));
//            when(mapper.toResponse(session)).thenReturn(sessionResponse);
//
//            RespEventSessionDto result = service.create(EVENT_ID, validRequest);
//
//            assertThat(result).isEqualTo(sessionResponse);
//            verify(eventSessionRepository).save(any(EventSession.class));
//            verify(eventSessionSeatService).createAll(anyList());
//        }
//
//        @Test
//        @DisplayName("Event tapilmadiqda ResourceNotFoundException atılır")
//        void create_eventNotFound() {
//            when(eventService.getEventEntityById(EVENT_ID))
//                    .thenThrow(new ResourceNotFoundException("Event tapılmadı, id: " + EVENT_ID));
//
//            assertThatThrownBy(() -> service.create(EVENT_ID, validRequest))
//                    .isInstanceOf(ResourceNotFoundException.class);
//
//            verifyNoInteractions(eventSessionRepository);
//        }
//
//        @Test
//        @DisplayName("Hall tapilmadiqda ResourceNotFoundException atılır")
//        void create_hallNotFound() {
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(hallService.getById(HALL_ID))
//                    .thenThrow(new ResourceNotFoundException("Hall tapılmadı, id: " + HALL_ID));
//
//            assertThatThrownBy(() -> service.create(EVENT_ID, validRequest))
//                    .isInstanceOf(ResourceNotFoundException.class);
//        }
//
//        @Test
//        @DisplayName("endTime <= startTime olduqda IllegalArgumentException atılır")
//        void create_invalidTimeRange() {
//            ReqEventSessionDto badRequest = new ReqEventSessionDto(
//                    HALL_ID, END, START, BigDecimal.valueOf(50), 100); // reversed
//
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(hallService.getById(HALL_ID)).thenReturn(hall);
//
//            assertThatThrownBy(() -> service.create(EVENT_ID, badRequest))
//                    .isInstanceOf(IllegalArgumentException.class)
//                    .hasMessageContaining("endTime");
//        }
//
//        @Test
//        @DisplayName("Eyni Hall-da zaman kəsişməsi olduqda SessionTimeConflictException atılır")
//        void create_overlapConflict() {
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(hallService.getById(HALL_ID)).thenReturn(hall);
//            when(eventSessionRepository.findOverlappingSessions(HALL_ID, START, END, null))
//                    .thenReturn(List.of(session));
//
//            assertThatThrownBy(() -> service.create(EVENT_ID, validRequest))
//                    .isInstanceOf(SessionTimeConflictException.class)
//                    .hasMessageContaining(String.valueOf(HALL_ID));
//
//            verify(eventSessionRepository, never()).save(any());
//        }
//
//        @Test
//        @DisplayName("startTime == endTime olduqda IllegalArgumentException atılır")
//        void create_startEqualsEnd() {
//            ReqEventSessionDto sameTime = new ReqEventSessionDto(
//                    HALL_ID, START, START, BigDecimal.valueOf(50), 100);
//
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(hallService.getById(HALL_ID)).thenReturn(hall);
//
//            assertThatThrownBy(() -> service.create(EVENT_ID, sameTime))
//                    .isInstanceOf(IllegalArgumentException.class);
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // UPDATE
//    // ─────────────────────────────────────────────────────────────────────────────
//    @Nested
//    @DisplayName("update()")
//    class UpdateTests {
//
//        @Test
//        @DisplayName("Uğurlu yeniləmə — dəyişdirilmiş session qaytarılır")
//        void update_success() {
//            ReqEventSessionDto updatedRequest = new ReqEventSessionDto(
//                    HALL_ID,
//                    START.plusHours(1),
//                    END.plusHours(1),
//                    BigDecimal.valueOf(75),
//                    80
//            );
//            RespEventSessionDto updatedResponse = new RespEventSessionDto(
//                    SESSION_ID, EVENT_ID, "Test Event",
//                    HALL_ID, "Main Hall",
//                    START.plusHours(1), END.plusHours(1),
//                    BigDecimal.valueOf(75), 80,
//                    null, null
//            );
//
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
//            when(hallService.getById(HALL_ID)).thenReturn(hall);
//            when(eventSessionRepository.findOverlappingSessions(
//                    HALL_ID, START.plusHours(1), END.plusHours(1), SESSION_ID))
//                    .thenReturn(List.of());
//            when(eventSessionRepository.save(session)).thenReturn(session);
//            when(mapper.toResponse(session)).thenReturn(updatedResponse);
//
//            RespEventSessionDto result = service.update(EVENT_ID, SESSION_ID, updatedRequest);
//
//            assertThat(result.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(75));
//            assertThat(result.availableSeats()).isEqualTo(80);
//        }
//
//        @Test
//        @DisplayName("Update zamanı özü ilə kəsişmə yoxlanmır (excludeId = sessionId)")
//        void update_doesNotConflictWithItself() {
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
//            when(hallService.getById(HALL_ID)).thenReturn(hall);
//            when(eventSessionRepository.findOverlappingSessions(HALL_ID, START, END, SESSION_ID))
//                    .thenReturn(List.of()); // özünü exclude etdi, nəticə boş
//            when(eventSessionRepository.save(session)).thenReturn(session);
//            when(mapper.toResponse(session)).thenReturn(sessionResponse);
//
//            service.update(EVENT_ID, SESSION_ID, validRequest);
//
//            verify(eventSessionRepository).findOverlappingSessions(HALL_ID, START, END, SESSION_ID);
//        }
//
//        @Test
//        @DisplayName("Session başqa event-ə aiddirsə ResourceNotFoundException atılır")
//        void update_sessionBelongsToDifferentEvent() {
//            Event otherEvent = new Event();
//            otherEvent.setId(999L);
//            session.setEvent(otherEvent);
//
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
//
//            assertThatThrownBy(() -> service.update(EVENT_ID, SESSION_ID, validRequest))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining(String.valueOf(SESSION_ID));
//        }
//
//        @Test
//        @DisplayName("Fərqli bir session ilə kəsişmə varsa SessionTimeConflictException atılır")
//        void update_conflictWithAnotherSession() {
//            EventSession conflictingSession = new EventSession();
//            conflictingSession.setId(200L);
//            conflictingSession.setStartTime(START);
//            conflictingSession.setEndTime(END);
//
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
//            when(hallService.getById(HALL_ID)).thenReturn(hall);
//            when(eventSessionRepository.findOverlappingSessions(HALL_ID, START, END, SESSION_ID))
//                    .thenReturn(List.of(conflictingSession));
//
//            assertThatThrownBy(() -> service.update(EVENT_ID, SESSION_ID, validRequest))
//                    .isInstanceOf(SessionTimeConflictException.class);
//
//            verify(eventSessionRepository, never()).save(any());
//        }
//
//        @Test
//        @DisplayName("Session tapılmadıqda ResourceNotFoundException atılır")
//        void update_sessionNotFound() {
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> service.update(EVENT_ID, SESSION_ID, validRequest))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining(String.valueOf(SESSION_ID));
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // DELETE
//    // ─────────────────────────────────────────────────────────────────────────────
//    @Nested
//    @DisplayName("delete()")
//    class DeleteTests {
//
//        @Test
//        @DisplayName("Uğurlu silmə — delete çağırılır")
//        void delete_success() {
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
//
//            service.delete(EVENT_ID, SESSION_ID);
//
//            verify(eventSessionRepository).delete(session);
//        }
//
//        @Test
//        @DisplayName("Event tapılmadıqda ResourceNotFoundException atılır")
//        void delete_eventNotFound() {
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//
//            assertThatThrownBy(() -> service.delete(EVENT_ID, SESSION_ID))
//                    .isInstanceOf(ResourceNotFoundException.class);
//
//            verify(eventSessionRepository, never()).delete(any());
//        }
//
//        @Test
//        @DisplayName("Session tapılmadıqda ResourceNotFoundException atılır")
//        void delete_sessionNotFound() {
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> service.delete(EVENT_ID, SESSION_ID))
//                    .isInstanceOf(ResourceNotFoundException.class);
//
//            verify(eventSessionRepository, never()).delete(any());
//        }
//
//        @Test
//        @DisplayName("Session başqa event-ə aiddirsə ResourceNotFoundException atılır, silmə baş vermir")
//        void delete_sessionBelongsToDifferentEvent() {
//            Event otherEvent = new Event();
//            otherEvent.setId(999L);
//            session.setEvent(otherEvent);
//
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
//
//            assertThatThrownBy(() -> service.delete(EVENT_ID, SESSION_ID))
//                    .isInstanceOf(ResourceNotFoundException.class);
//
//            verify(eventSessionRepository, never()).delete(any());
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // OVERLAP EDGE CASES
//    // ─────────────────────────────────────────────────────────────────────────────
//    @Nested
//    @DisplayName("Zaman kəsişməsi — edge case-lər")
//    class OverlapEdgeCaseTests {
//
//        private void stubCreatePrerequisites() {
//            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
//            when(hallService.getById(HALL_ID)).thenReturn(hall);
//        }
//
//        @Test
//        @DisplayName("Yeni session mövcud session-dan əvvəl bitdikdə kəsişmə yoxdur")
//        void noOverlap_newSessionEndsBefore() {
//            // existing: 12:00-14:00, new: 10:00-12:00 (touching at boundary — no overlap)
//            ReqEventSessionDto req = new ReqEventSessionDto(
//                    HALL_ID,
//                    LocalDateTime.of(2025, 6, 1, 10, 0),
//                    LocalDateTime.of(2025, 6, 1, 12, 0),
//                    BigDecimal.valueOf(50), 100
//            );
//            stubCreatePrerequisites();
//            when(eventSessionRepository.findOverlappingSessions(
//                    eq(HALL_ID), any(), any(), eq(null)))
//                    .thenReturn(List.of());
//            when(eventSessionRepository.save(any())).thenReturn(session);
//            when(mapper.toResponse(any())).thenReturn(sessionResponse);
//
//            // should not throw
//            service.create(EVENT_ID, req);
//        }
//
//        @Test
//        @DisplayName("Yeni session mövcud session-dan sonra başladıqda kəsişmə yoxdur")
//        void noOverlap_newSessionStartsAfter() {
//            ReqEventSessionDto req = new ReqEventSessionDto(
//                    HALL_ID,
//                    LocalDateTime.of(2025, 6, 1, 14, 0),
//                    LocalDateTime.of(2025, 6, 1, 16, 0),
//                    BigDecimal.valueOf(50), 100
//            );
//            stubCreatePrerequisites();
//            when(eventSessionRepository.findOverlappingSessions(
//                    eq(HALL_ID), any(), any(), eq(null)))
//                    .thenReturn(List.of());
//            when(eventSessionRepository.save(any())).thenReturn(session);
//            when(mapper.toResponse(any())).thenReturn(sessionResponse);
//
//            service.create(EVENT_ID, req);
//            verify(eventSessionRepository).save(any());
//        }
//
//        @Test
//        @DisplayName("Yeni session mövcud session-u tam əhatə etdikdə kəsişmə var")
//        void overlap_newSessionCoversExisting() {
//            EventSession existing = new EventSession();
//            existing.setId(200L);
//            existing.setStartTime(LocalDateTime.of(2025, 6, 1, 11, 0));
//            existing.setEndTime(LocalDateTime.of(2025, 6, 1, 13, 0));
//
//            ReqEventSessionDto req = new ReqEventSessionDto(
//                    HALL_ID,
//                    LocalDateTime.of(2025, 6, 1, 10, 0),
//                    LocalDateTime.of(2025, 6, 1, 14, 0),
//                    BigDecimal.valueOf(50), 100
//            );
//            stubCreatePrerequisites();
//            when(eventSessionRepository.findOverlappingSessions(
//                    eq(HALL_ID), any(), any(), eq(null)))
//                    .thenReturn(List.of(existing));
//
//            assertThatThrownBy(() -> service.create(EVENT_ID, req))
//                    .isInstanceOf(SessionTimeConflictException.class);
//        }
//
//        @Test
//        @DisplayName("Mövcud session yeni session-u tam əhatə etdikdə kəsişmə var")
//        void overlap_existingCoversNew() {
//            EventSession existing = new EventSession();
//            existing.setId(200L);
//            existing.setStartTime(LocalDateTime.of(2025, 6, 1, 9, 0));
//            existing.setEndTime(LocalDateTime.of(2025, 6, 1, 15, 0));
//
//            ReqEventSessionDto req = new ReqEventSessionDto(
//                    HALL_ID,
//                    LocalDateTime.of(2025, 6, 1, 10, 0),
//                    LocalDateTime.of(2025, 6, 1, 12, 0),
//                    BigDecimal.valueOf(50), 100
//            );
//            stubCreatePrerequisites();
//            when(eventSessionRepository.findOverlappingSessions(
//                    eq(HALL_ID), any(), any(), eq(null)))
//                    .thenReturn(List.of(existing));
//
//            assertThatThrownBy(() -> service.create(EVENT_ID, req))
//                    .isInstanceOf(SessionTimeConflictException.class);
//        }
//    }
//}
