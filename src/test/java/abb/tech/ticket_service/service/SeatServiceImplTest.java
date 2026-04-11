package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.SeatUpdateRequest;
import abb.tech.ticket_service.mapper.SeatMapper;
import abb.tech.ticket_service.model.Seat;
import abb.tech.ticket_service.repository.SeatRepository;
import abb.tech.ticket_service.service.impl.SeatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatMapper seatMapper;

    @InjectMocks
    private SeatServiceImpl seatService;

    private Seat seat;
    private static final Long SEAT_ID = 1L;

    @BeforeEach
    void setUp() {
        seat = new Seat();
        seat.setId(SEAT_ID);
        seat.setSeatNumber(10);
    }

    @Nested
    @DisplayName("getById Testləri")
    class GetByIdTests {

        @Test
        @DisplayName("Uğurlu — Seat tapılmalıdır")
        void getById_success() {
            when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));

            Seat result = seatService.getById(SEAT_ID);

            assertNotNull(result);
            assertEquals(SEAT_ID, result.getId());
            verify(seatRepository).findById(SEAT_ID);
        }

        @Test
        @DisplayName("Xəta — Seat tapılmadıqda NOT_FOUND (404) atmalıdır")
        void getById_notFound() {
            when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> seatService.getById(SEAT_ID));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertTrue(exception.getReason().contains("not found"));
        }
    }

    @Nested
    @DisplayName("Axtarış və Yaratma Testləri")
    class SearchAndCreateTests {

        @Test
        @DisplayName("Uğurlu — HallId-yə görə bütün oturacaqları qaytarmalıdır")
        void findAllByHallId_success() {
            Long hallId = 5L;
            when(seatRepository.findAllByHallId(hallId)).thenReturn(List.of(seat));

            List<Seat> results = seatService.findAllByHallId(hallId);

            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            verify(seatRepository).findAllByHallId(hallId);
        }

        @Test
        @DisplayName("Uğurlu — Yeni seat yadda saxlanılmalıdır")
        void create_success() {
            when(seatRepository.save(seat)).thenReturn(seat);

            Seat result = seatService.create(seat);

            assertNotNull(result);
            verify(seatRepository).save(seat);
        }
    }

    @Nested
    @DisplayName("Update və Delete Testləri")
    class UpdateDeleteTests {

        @Test
        @DisplayName("Uğurlu — Seat məlumatları yenilənməlidir")
        void updateSeat_success() {
            SeatUpdateRequest request = new SeatUpdateRequest();
            when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));

            seatService.updateSeat(SEAT_ID, request);

            verify(seatMapper).updateSeat(seat, request);
            verify(seatRepository).save(seat);
        }

        @Test
        @DisplayName("Uğurlu — Seat silinməlidir")
        void deleteSeat_success() {
            when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));

            seatService.deleteSeat(SEAT_ID);

            verify(seatRepository).delete(seat);
        }
    }
}