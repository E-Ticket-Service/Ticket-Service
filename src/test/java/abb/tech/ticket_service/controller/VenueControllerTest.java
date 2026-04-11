package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.VenueCreationRequest;
import abb.tech.ticket_service.dto.request.VenueUpdateRequest;
import abb.tech.ticket_service.dto.response.VenueResponse;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.VenueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VenueControllerTest {

    @Mock
    private VenueService venueService;

    @InjectMocks
    private VenueController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long VENUE_ID = 1L;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("GET /venues/{id}")
    class GetVenueTests {

        @Test
        @DisplayName("200 OK — Uğurlu")
        void getVenueById_success() throws Exception {
            VenueResponse response = mock(VenueResponse.class);
            when(response.getId()).thenReturn(VENUE_ID);
            when(venueService.getVenueById(VENUE_ID)).thenReturn(response);

            mockMvc.perform(get("/venues/{id}", VENUE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(VENUE_ID));
        }

        @Test
        @DisplayName("404 Not Found — Venue tapılmadıqda")
        void getVenueById_returns404() throws Exception {
            when(venueService.getVenueById(VENUE_ID))
                    .thenThrow(new ResourceNotFoundException("Venue tapılmadı"));

            mockMvc.perform(get("/venues/{id}", VENUE_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /venues")
    class CreateVenueTests {

        @Test
        @DisplayName("201 Created — Uğurlu yaratma")
        void createVenue_success() throws Exception {
            // @NotNull sahələri (name, city, address) doldururuq
            VenueCreationRequest request = new VenueCreationRequest(
                    "Baku Crystal Hall",
                    "Baku",
                    "State Flag Square",
                    "Famous concert arena"
            );

            doNothing().when(venueService).createVenue(any(VenueCreationRequest.class));

            mockMvc.perform(post("/venues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(venueService).createVenue(any(VenueCreationRequest.class));
        }

        @Test
        @DisplayName("400 Bad Request — Validation xətası")
        void createVenue_returns400_whenInvalid() throws Exception {
            mockMvc.perform(post("/venues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /venues/{id}")
    class UpdateVenueTests {

        @Test
        @DisplayName("200 OK — Uğurlu yeniləmə")
        void updateVenue_success() throws Exception {
            VenueUpdateRequest request = new VenueUpdateRequest();

            doNothing().when(venueService).updateVenue(eq(VENUE_ID), any(VenueUpdateRequest.class));

            mockMvc.perform(put("/venues/{id}", VENUE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(venueService).updateVenue(eq(VENUE_ID), any(VenueUpdateRequest.class));
        }
    }
}