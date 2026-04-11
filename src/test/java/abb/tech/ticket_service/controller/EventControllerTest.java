package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.ReqEventDto;
import abb.tech.ticket_service.dto.response.RespEventDto;
import abb.tech.ticket_service.enums.EventCategory;
import abb.tech.ticket_service.enums.EventStatus;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.EventService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long EVENT_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    private ReqEventDto createValidReq() {
        return new ReqEventDto(
                "Test Event Name",
                "This is a long enough description for the event.",
                18,
                120.0,
                "AZE",
                List.of(UUID.randomUUID()),
                EventCategory.CONCERT,
                EventStatus.PUBLISHED
        );
    }

    @Nested
    @DisplayName("POST /events/create")
    class CreateEventTests {

        @Test
        @DisplayName("201 Created — uğurlu yaratma")
        void createEvent_returns201() throws Exception {
            ReqEventDto validRequest = createValidReq();
            RespEventDto response = mock(RespEventDto.class);

            when(response.id()).thenReturn(EVENT_ID);
            when(eventService.createEvent(any())).thenReturn(response);

            mockMvc.perform(post("/events/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(EVENT_ID));
        }
    }

    @Nested
    @DisplayName("PUT /events/{id}")
    class UpdateEventTests {

        @Test
        @DisplayName("200 OK — uğurlu yeniləmə")
        void updateEvent_returns200() throws Exception {
            ReqEventDto validRequest = createValidReq();
            RespEventDto response = mock(RespEventDto.class);

            when(eventService.updateEvent(eq(EVENT_ID), any())).thenReturn(response);

            mockMvc.perform(put("/events/{id}", EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /events/by-id")
    class GetEventByIdTests {

        @Test
        @DisplayName("202 Accepted — RequestParam ilə uğurlu tapılma")
        void getEventById_returns202() throws Exception {
            RespEventDto response = mock(RespEventDto.class);
            when(response.id()).thenReturn(EVENT_ID);
            when(eventService.getEventById(EVENT_ID)).thenReturn(response);

            mockMvc.perform(get("/events/by-id")
                            .param("id", EVENT_ID.toString()))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.id").value(EVENT_ID));
        }

        @Test
        @DisplayName("404 Not Found — event tapılmadıqda")
        void getEventById_returns404() throws Exception {
            when(eventService.getEventById(EVENT_ID))
                    .thenThrow(new ResourceNotFoundException("Event tapılmadı"));

            mockMvc.perform(get("/events/by-id")
                            .param("id", EVENT_ID.toString()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /events")
    class GetAllEventsTests {

        @Test
        @DisplayName("202 Accepted — bütün eventlərin siyahısı")
        void getAllEvents_returns202() throws Exception {
            RespEventDto response = mock(RespEventDto.class);
            when(eventService.getAllEvents()).thenReturn(List.of(response));

            mockMvc.perform(get("/events"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("DELETE /events/{id}")
    class DeleteEventTests {

        @Test
        @DisplayName("204 No Content — uğurlu silmə")
        void deleteEvent_returns204() throws Exception {
            doNothing().when(eventService).deleteEvent(EVENT_ID);

            mockMvc.perform(delete("/events/{id}", EVENT_ID))
                    .andExpect(status().isNoContent());

            verify(eventService).deleteEvent(EVENT_ID);
        }
    }
}