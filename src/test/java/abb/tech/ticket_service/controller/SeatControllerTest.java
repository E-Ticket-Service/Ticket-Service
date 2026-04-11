package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.SeatUpdateRequest;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.SeatService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SeatControllerTest {

    @Mock
    private SeatService seatService;

    @InjectMocks
    private SeatController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long SEAT_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("PUT /seats/{id}")
    class UpdateSeatTests {

        @Test
        @DisplayName("200 OK — uğurlu yeniləmə")
        void updateSeat_success() throws Exception {
            SeatUpdateRequest request = new SeatUpdateRequest();
            doNothing().when(seatService).updateSeat(eq(SEAT_ID), any(SeatUpdateRequest.class));

            mockMvc.perform(put("/seats/{id}", SEAT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(seatService).updateSeat(eq(SEAT_ID), any(SeatUpdateRequest.class));
        }

        @Test
        @DisplayName("404 Not Found — seat tapılmadıqda")
        void updateSeat_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Seat tapılmadı"))
                    .when(seatService).updateSeat(eq(SEAT_ID), any());

            mockMvc.perform(put("/seats/{id}", SEAT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /seats/{id}")
    class DeleteSeatTests {

        @Test
        @DisplayName("204 No Content — uğurlu silmə")
        void deleteSeat_success() throws Exception {
            doNothing().when(seatService).deleteSeat(SEAT_ID);

            mockMvc.perform(delete("/seats/{id}", SEAT_ID))
                    .andExpect(status().isNoContent());

            verify(seatService).deleteSeat(SEAT_ID);
        }

        @Test
        @DisplayName("404 Not Found — silinəcək seat tapılmadıqda")
        void deleteSeat_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Seat tapılmadı"))
                    .when(seatService).deleteSeat(SEAT_ID);

            mockMvc.perform(delete("/seats/{id}", SEAT_ID))
                    .andExpect(status().isNotFound());
        }
    }
}