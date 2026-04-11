package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.HallCreationRequest;
import abb.tech.ticket_service.dto.request.HallUpdateRequest;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.HallService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HallControllerTest {

    @Mock
    private HallService hallService;

    @InjectMocks
    private HallController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long HALL_ID = 1L;
    private static final Long VENUE_ID = 10L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("POST /halls/venue/{venueId}")
    class CreateHallTests {

        @Test
        @DisplayName("201 Created — uğurlu yaratma")
        void createHalls_returns201() throws Exception {
            HallCreationRequest request = new HallCreationRequest(
                    "Main Hall",
                    100,
                    true,
                    List.of()
            );

            doNothing().when(hallService).createHall(any(HallCreationRequest.class), eq(VENUE_ID));

            mockMvc.perform(post("/halls/venue/{venueId}", VENUE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(hallService).createHall(any(HallCreationRequest.class), eq(VENUE_ID));
        }

        @Test
        @DisplayName("400 Bad Request — validation xətası")
        void createHalls_returns400_whenInvalid() throws Exception {
            mockMvc.perform(post("/halls/venue/{venueId}", VENUE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /halls/{id}")
    class UpdateHallTests {

        @Test
        @DisplayName("200 OK — uğurlu yeniləmə")
        void updateHall_returns200() throws Exception {
            HallUpdateRequest request = mock(HallUpdateRequest.class);

            doNothing().when(hallService).updateHall(eq(HALL_ID), any());

            mockMvc.perform(put("/halls/{id}", HALL_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("404 Not Found — hall tapılmadıqda")
        void updateHall_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Hall tapılmadı"))
                    .when(hallService).updateHall(eq(HALL_ID), any());

            mockMvc.perform(put("/halls/{id}", HALL_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /halls/{id}")
    class DeleteHallTests {

        @Test
        @DisplayName("204 No Content — uğurlu silmə")
        void deleteHall_returns204() throws Exception {
            doNothing().when(hallService).deleteHall(HALL_ID);

            mockMvc.perform(delete("/halls/{id}", HALL_ID))
                    .andExpect(status().isNoContent());

            verify(hallService).deleteHall(HALL_ID);
        }

        @Test
        @DisplayName("404 Not Found — silinəcək hall tapılmadıqda")
        void deleteHall_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Hall tapılmadı"))
                    .when(hallService).deleteHall(HALL_ID);

            mockMvc.perform(delete("/halls/{id}", HALL_ID))
                    .andExpect(status().isNotFound());
        }
    }
}