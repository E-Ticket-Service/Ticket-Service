package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.VenueCreationRequest;
import abb.tech.ticket_service.dto.request.VenueUpdateRequest;
import abb.tech.ticket_service.dto.response.VenueResponse;
import abb.tech.ticket_service.mapper.VenueMapper;
import abb.tech.ticket_service.model.Venue;
import abb.tech.ticket_service.repository.VenueRepository;
import abb.tech.ticket_service.service.impl.VenueServiceImpl;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private VenueServiceImpl venueService;

    private Venue venue;
    private static final Long VENUE_ID = 1L;

    @BeforeEach
    void setUp() {
        venue = new Venue();
        venue.setId(VENUE_ID);
        venue.setName("Heydar Aliyev Palace");
        venue.setAddress("Baku, Azerbaijan");
    }

    @Nested
    @DisplayName("Giriş və Axtarış Testləri")
    class RetrievalTests {

        @Test
        @DisplayName("Uğurlu — Venue tapılmalı və response-a çevrilməlidir")
        void getVenueById_success() {
            VenueResponse mockResponse = mock(VenueResponse.class);

            when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));
            when(venueMapper.toResponse(venue)).thenReturn(mockResponse);

            VenueResponse result = venueService.getVenueById(VENUE_ID);

            assertNotNull(result);
            verify(venueRepository).findById(VENUE_ID);
            verify(venueMapper).toResponse(venue);
        }

        @Test
        @DisplayName("Xəta — Venue tapılmadıqda 404 xətası atmalıdır")
        void getById_notFound() {
            when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> venueService.getById(VENUE_ID));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Venue not found.", exception.getReason());
        }
    }

    @Nested
    @DisplayName("Yaratma, Yeniləmə və Silmə Testləri")
    class ModificationTests {

        @Test
        @DisplayName("Uğurlu — Yeni venue yaradılmalıdır")
        void createVenue_success() {
            VenueCreationRequest request = new VenueCreationRequest();
            when(venueMapper.toEntity(request)).thenReturn(venue);

            venueService.createVenue(request);

            verify(venueRepository).save(venue);
        }

        @Test
        @DisplayName("Uğurlu — Mövcud venue məlumatları yenilənməlidir")
        void updateVenue_success() {
            VenueUpdateRequest request = new VenueUpdateRequest();
            when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));

            venueService.updateVenue(VENUE_ID, request);

            verify(venueMapper).updateVenue(venue, request);
            verify(venueRepository).save(venue);
        }

        @Test
        @DisplayName("Uğurlu — Venue sistemdən silinməlidir")
        void deleteVenue_success() {
            when(venueRepository.findById(VENUE_ID)).thenReturn(Optional.of(venue));

            venueService.deleteVenue(VENUE_ID);

            verify(venueRepository).delete(venue);
        }
    }
}