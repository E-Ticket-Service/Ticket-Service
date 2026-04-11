package abb.tech.ticket_service.service;

import abb.tech.ticket_service.model.EventSessionSeat;
import abb.tech.ticket_service.repository.EventSessionSeatRepository;
import abb.tech.ticket_service.service.impl.EventSessionSeatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSessionSeatServiceImplTest {

    @Mock
    private EventSessionSeatRepository eventSessionSeatRepository;

    @InjectMocks
    private EventSessionSeatServiceImpl eventSessionSeatService;

    private static final Long SESSION_ID = 10L;
    private static final Long SEAT_ID = 20L;

    @Nested
    @DisplayName("findByEventSessionIdAndSeatId Testləri")
    class FindTests {

        @Test
        @DisplayName("Uğurlu — Sessiya və yer ID-sinə görə tapılmalıdır")
        void findByEventSessionIdAndSeatId_success() {
            EventSessionSeat seat = new EventSessionSeat();
            when(eventSessionSeatRepository.findByEventSessionIdAndSeatId(SESSION_ID, SEAT_ID))
                    .thenReturn(Optional.of(seat));

            EventSessionSeat result = eventSessionSeatService.findByEventSessionIdAndSeatId(SESSION_ID, SEAT_ID);

            assertNotNull(result);
            assertEquals(seat, result);
            verify(eventSessionSeatRepository).findByEventSessionIdAndSeatId(SESSION_ID, SEAT_ID);
        }

        @Test
        @DisplayName("Xəta — Tapılmadıqda IllegalStateException atmalıdır")
        void findByEventSessionIdAndSeatId_notFound() {
            when(eventSessionSeatRepository.findByEventSessionIdAndSeatId(SESSION_ID, SEAT_ID))
                    .thenReturn(Optional.empty());

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> eventSessionSeatService.findByEventSessionIdAndSeatId(SESSION_ID, SEAT_ID));

            assertEquals("Seat not found for this session", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Yaratma (Create) Testləri")
    class CreateTests {

        @Test
        @DisplayName("Uğurlu — Tək obyekt yadda saxlanılmalıdır")
        void create_success() {
            EventSessionSeat seat = new EventSessionSeat();
            eventSessionSeatService.create(seat);
            verify(eventSessionSeatRepository).save(seat);
        }

        @Test
        @DisplayName("Uğurlu — Toplu (List) şəkildə yadda saxlanılmalıdır")
        void createAll_success() {
            List<EventSessionSeat> seats = List.of(new EventSessionSeat(), new EventSessionSeat());

            eventSessionSeatService.createAll(seats);

            verify(eventSessionSeatRepository).saveAll(seats);
        }
    }
}
