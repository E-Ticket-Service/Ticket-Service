package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.ReqEventSessionDto;
import abb.tech.ticket_service.dto.response.RespEventSessionDto;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.exception.SessionTimeConflictException;
import abb.tech.ticket_service.mapper.EventSessionMapper;
import abb.tech.ticket_service.model.Event;
import abb.tech.ticket_service.model.EventSession;
import abb.tech.ticket_service.model.Hall;
import abb.tech.ticket_service.model.Seat;
import abb.tech.ticket_service.repository.EventSessionRepository;
import abb.tech.ticket_service.service.impl.EventSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock private EventSessionRepository eventSessionRepository;
    @Mock private EventService eventService;
    @Mock private HallService hallService;
    @Mock private SeatService seatService;
    @Mock private EventSessionSeatService eventSessionSeatService;
    @Mock private EventSessionMapper mapper;

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
        ReflectionTestUtils.setField(event, "id", EVENT_ID);
        event.setName("Test Event");

        hall = new Hall();
        ReflectionTestUtils.setField(hall, "id", HALL_ID);
        hall.setName("Main Hall");

        session = new EventSession();
        ReflectionTestUtils.setField(session, "id", SESSION_ID);
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

    @Test
    @DisplayName("findById() — tapıldıqda entity qaytarılmalıdır")
    void findById_success() {
        when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        EventSession result = service.findById(SESSION_ID);
        assertThat(result).isEqualTo(session);
    }

    @Nested
    @DisplayName("getById() Testləri")
    class GetByIdTests {
        @Test
        @DisplayName("Uğurlu əldəetmə")
        void getById_success() {
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
            when(mapper.toResponse(session)).thenReturn(sessionResponse);

            RespEventSessionDto result = service.getById(EVENT_ID, SESSION_ID);

            assertThat(result).isEqualTo(sessionResponse);
            verify(eventService).getEventById(EVENT_ID);
        }

        @Test
        @DisplayName("Session başqa event-ə aiddirsə ResourceNotFoundException")
        void getById_wrongEvent() {
            Event otherEvent = new Event();
            ReflectionTestUtils.setField(otherEvent, "id", 99L);
            session.setEvent(otherEvent);

            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> service.getById(EVENT_ID, SESSION_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllByEvent() Testləri")
    class GetAllByEventTests {
        @Test
        @DisplayName("Event-ə aid bütün sessionlar qaytarılır")
        void getAllByEvent_success() {
            when(eventSessionRepository.findByEventId(EVENT_ID)).thenReturn(List.of(session));
            when(mapper.toResponse(session)).thenReturn(sessionResponse);

            List<RespEventSessionDto> result = service.getAllByEvent(EVENT_ID);

            assertThat(result).hasSize(1);
            verify(eventService).getEventById(EVENT_ID);
        }
    }

    @Nested
    @DisplayName("create() Testləri")
    class CreateTests {
        @Test
        @DisplayName("Uğurlu yaratma")
        void create_success() {
            Seat seat1 = new Seat();
            ReflectionTestUtils.setField(seat1, "id", 1L);
            seat1.setExtraPrice(BigDecimal.ZERO);

            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
            when(hallService.getById(HALL_ID)).thenReturn(hall);
            when(eventSessionRepository.findOverlappingSessions(any(), any(), any(), eq(null)))
                    .thenReturn(List.of());
            when(eventSessionRepository.save(any(EventSession.class))).thenReturn(session);
            when(seatService.findAllByHallId(HALL_ID)).thenReturn(List.of(seat1));
            when(mapper.toResponse(session)).thenReturn(sessionResponse);

            RespEventSessionDto result = service.create(EVENT_ID, validRequest);

            assertThat(result).isEqualTo(sessionResponse);
            verify(eventSessionSeatService).createAll(anyList());
        }

        @Test
        @DisplayName("Zaman kəsişməsi olduqda SessionTimeConflictException")
        void create_overlapConflict() {
            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
            when(hallService.getById(HALL_ID)).thenReturn(hall);
            when(eventSessionRepository.findOverlappingSessions(any(), any(), any(), any()))
                    .thenReturn(List.of(session));

            assertThatThrownBy(() -> service.create(EVENT_ID, validRequest))
                    .isInstanceOf(SessionTimeConflictException.class);
        }

        @Test
        @DisplayName("startTime >= endTime olduqda IllegalArgumentException")
        void create_invalidTime() {
            ReqEventSessionDto invalidReq = new ReqEventSessionDto(HALL_ID, END, START, BigDecimal.TEN, 50);
            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
            when(hallService.getById(HALL_ID)).thenReturn(hall);

            assertThatThrownBy(() -> service.create(EVENT_ID, invalidReq))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("update() Testləri")
    class UpdateTests {
        @Test
        @DisplayName("Uğurlu yeniləmə")
        void update_success() {
            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
            when(hallService.getById(HALL_ID)).thenReturn(hall);
            when(eventSessionRepository.findOverlappingSessions(eq(HALL_ID), any(), any(), eq(SESSION_ID)))
                    .thenReturn(List.of());
            when(eventSessionRepository.save(any())).thenReturn(session);
            when(mapper.toResponse(session)).thenReturn(sessionResponse);

            service.update(EVENT_ID, SESSION_ID, validRequest);

            verify(eventSessionRepository).save(session);
        }
    }

    @Nested
    @DisplayName("delete() Testləri")
    class DeleteTests {
        @Test
        @DisplayName("Uğurlu silmə")
        void delete_success() {
            when(eventService.getEventEntityById(EVENT_ID)).thenReturn(event);
            when(eventSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

            service.delete(EVENT_ID, SESSION_ID);

            verify(eventSessionRepository).delete(session);
        }
    }
}