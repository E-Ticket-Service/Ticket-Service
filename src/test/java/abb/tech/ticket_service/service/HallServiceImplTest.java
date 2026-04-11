package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.HallCreationRequest;
import abb.tech.ticket_service.dto.request.HallUpdateRequest;
import abb.tech.ticket_service.mapper.HallMapper;
import abb.tech.ticket_service.model.Block;
import abb.tech.ticket_service.model.Hall;
import abb.tech.ticket_service.model.Section;
import abb.tech.ticket_service.model.Venue;
import abb.tech.ticket_service.repository.HallRepository;
import abb.tech.ticket_service.service.VenueService;
import abb.tech.ticket_service.service.impl.HallServiceImpl;
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
class HallServiceImplTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private HallMapper hallMapper;

    @Mock
    private VenueService venueService;

    @InjectMocks
    private HallServiceImpl hallService;

    private static final Long HALL_ID = 1L;
    private static final Long VENUE_ID = 10L;
    private Hall hall;

    @BeforeEach
    void setUp() {
        hall = new Hall();
        hall.setId(HALL_ID);
    }

    @Nested
    @DisplayName("getById Testləri")
    class GetByIdTests {

        @Test
        @DisplayName("Uğurlu — Hall tapıldıqda qaytarmalıdır")
        void getById_success() {
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));

            Hall result = hallService.getById(HALL_ID);

            assertNotNull(result);
            assertEquals(hall, result);
            verify(hallRepository).findById(HALL_ID);
        }

        @Test
        @DisplayName("Xəta — Hall tapılmadıqda 404 atmalıdır")
        void getById_notFound() {
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> hallService.getById(HALL_ID));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("createHall Testləri")
    class CreateHallTests {

        @Test
        @DisplayName("Uğurlu — Hall, Section və Block asılılıqları düzgün qurulmalıdır")
        void createHall_success() {
            HallCreationRequest request = mock(HallCreationRequest.class);
            Venue venue = new Venue();

            Block block = new Block();
            Section section = new Section();
            section.setBlocks(List.of(block));
            hall.setSections(List.of(section));

            when(venueService.getById(VENUE_ID)).thenReturn(venue);
            when(hallMapper.toEntity(request)).thenReturn(hall);

            hallService.createHall(request, VENUE_ID);

            assertEquals(venue, hall.getVenue());
            assertEquals(hall, section.getHall());
            assertEquals(section, block.getSection());

            verify(hallRepository).save(hall);
        }
    }

    @Nested
    @DisplayName("updateHall Testləri")
    class UpdateHallTests {

        @Test
        @DisplayName("Uğurlu — Hall tapılmalı və yenilənməlidir")
        void updateHall_success() {
            HallUpdateRequest request = mock(HallUpdateRequest.class);
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));

            hallService.updateHall(HALL_ID, request);

            verify(hallMapper).updateHall(hall, request);
            verify(hallRepository).save(hall);
        }
    }

    @Nested
    @DisplayName("deleteHall Testləri")
    class DeleteHallTests {

        @Test
        @DisplayName("Uğurlu — Mövcud Hall silinməlidir")
        void deleteHall_success() {
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));

            hallService.deleteHall(HALL_ID);

            verify(hallRepository).delete(hall);
        }

        @Test
        @DisplayName("Xəta — Tapılmayan Hall silinə bilməz")
        void deleteHall_notFound() {
            when(hallRepository.findById(HALL_ID)).thenReturn(Optional.empty());

            assertThrows(ResponseStatusException.class, () -> hallService.deleteHall(HALL_ID));
            verify(hallRepository, never()).delete(any());
        }
    }
}