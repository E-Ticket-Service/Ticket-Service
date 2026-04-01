package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.ReqEventSessionDto;
import abb.tech.ticket_service.dto.response.RespEventSessionDto;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.exception.SessionTimeConflictException;
import abb.tech.ticket_service.service.EventSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class EventSessionControllerTest {

    @Mock
    private EventSessionService eventSessionService;

    @InjectMocks
    private EventSessionController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long EVENT_ID   = 1L;
    private static final Long SESSION_ID = 100L;
    private static final Long HALL_ID    = 10L;

    private static final LocalDateTime START = LocalDateTime.of(2025, 6, 1, 10, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2025, 6, 1, 12, 0);

    private ReqEventSessionDto validRequest;
    private RespEventSessionDto sessionResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        validRequest = new ReqEventSessionDto(HALL_ID, START, END, BigDecimal.valueOf(50), 100);

        sessionResponse = new RespEventSessionDto(
                SESSION_ID, EVENT_ID, "Test Event",
                HALL_ID, "Main Hall",
                START, END,
                BigDecimal.valueOf(50), 100,
                null, null
        );
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // POST /api/ticket/events/{eventId}/sessions
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/ticket/events/{eventId}/sessions")
    class CreateEndpoint {

        @Test
        @DisplayName("201 Created — valid request")
        void create_returns201() throws Exception {
            when(eventSessionService.create(eq(EVENT_ID), any())).thenReturn(sessionResponse);

            mockMvc.perform(post("/api/ticket/events/{eventId}/sessions", EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(SESSION_ID))
                    .andExpect(jsonPath("$.eventId").value(EVENT_ID))
                    .andExpect(jsonPath("$.hallId").value(HALL_ID));
        }

        @Test
        @DisplayName("404 Not Found — event mövcud deyil")
        void create_returns404_whenEventNotFound() throws Exception {
            when(eventSessionService.create(eq(EVENT_ID), any()))
                    .thenThrow(new ResourceNotFoundException("Event tapılmadı, id: " + EVENT_ID));

            mockMvc.perform(post("/api/ticket/events/{eventId}/sessions", EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Event tapılmadı, id: " + EVENT_ID));
        }

        @Test
        @DisplayName("409 Conflict — zaman kəsişməsi")
        void create_returns409_onTimeConflict() throws Exception {
            when(eventSessionService.create(eq(EVENT_ID), any()))
                    .thenThrow(new SessionTimeConflictException("Kəsişmə mövcuddur"));

            mockMvc.perform(post("/api/ticket/events/{eventId}/sessions", EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("400 Bad Request — hallId null")
        void create_returns400_whenHallIdNull() throws Exception {
            ReqEventSessionDto badReq = new ReqEventSessionDto(
                    null, START, END, BigDecimal.valueOf(50), 100);

            mockMvc.perform(post("/api/ticket/events/{eventId}/sessions", EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badReq)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — availableSeats sıfırdır")
        void create_returns400_whenSeatsZero() throws Exception {
            ReqEventSessionDto badReq = new ReqEventSessionDto(
                    HALL_ID, START, END, BigDecimal.valueOf(50), 0);

            mockMvc.perform(post("/api/ticket/events/{eventId}/sessions", EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badReq)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — basePrice mənfidir")
        void create_returns400_whenNegativePrice() throws Exception {
            ReqEventSessionDto badReq = new ReqEventSessionDto(
                    HALL_ID, START, END, BigDecimal.valueOf(-5), 100);

            mockMvc.perform(post("/api/ticket/events/{eventId}/sessions", EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badReq)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // PUT /api/ticket/events/{eventId}/sessions/{sessionId}
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/ticket/events/{eventId}/sessions/{sessionId}")
    class UpdateEndpoint {

        @Test
        @DisplayName("200 OK — uğurlu yeniləmə")
        void update_returns200() throws Exception {
            when(eventSessionService.update(eq(EVENT_ID), eq(SESSION_ID), any()))
                    .thenReturn(sessionResponse);

            mockMvc.perform(put("/api/ticket/events/{eventId}/sessions/{sessionId}", EVENT_ID, SESSION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(SESSION_ID));
        }

        @Test
        @DisplayName("404 Not Found — session mövcud deyil")
        void update_returns404_whenSessionNotFound() throws Exception {
            when(eventSessionService.update(eq(EVENT_ID), eq(SESSION_ID), any()))
                    .thenThrow(new ResourceNotFoundException("EventSession tapılmadı, id: " + SESSION_ID));

            mockMvc.perform(put("/api/ticket/events/{eventId}/sessions/{sessionId}", EVENT_ID, SESSION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("409 Conflict — başqa session ilə kəsişmə")
        void update_returns409_onConflict() throws Exception {
            when(eventSessionService.update(eq(EVENT_ID), eq(SESSION_ID), any()))
                    .thenThrow(new SessionTimeConflictException("Kəsişmə var"));

            mockMvc.perform(put("/api/ticket/events/{eventId}/sessions/{sessionId}", EVENT_ID, SESSION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GET /api/ticket/events/{eventId}/sessions/{sessionId}
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/ticket/events/{eventId}/sessions/{sessionId}")
    class GetByIdEndpoint {

        @Test
        @DisplayName("200 OK — session tapıldı")
        void getById_returns200() throws Exception {
            when(eventSessionService.getById(EVENT_ID, SESSION_ID)).thenReturn(sessionResponse);

            mockMvc.perform(get("/api/ticket/events/{eventId}/sessions/{sessionId}", EVENT_ID, SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(SESSION_ID))
                    .andExpect(jsonPath("$.hallName").value("Main Hall"));
        }

        @Test
        @DisplayName("404 Not Found — session tapılmadı")
        void getById_returns404() throws Exception {
            when(eventSessionService.getById(EVENT_ID, SESSION_ID))
                    .thenThrow(new ResourceNotFoundException("EventSession tapılmadı"));

            mockMvc.perform(get("/api/ticket/events/{eventId}/sessions/{sessionId}", EVENT_ID, SESSION_ID))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GET /api/ticket/events/{eventId}/sessions
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/ticket/events/{eventId}/sessions")
    class GetAllEndpoint {

        @Test
        @DisplayName("200 OK — session list qaytarılır")
        void getAll_returns200WithList() throws Exception {
            RespEventSessionDto r2 = new RespEventSessionDto(
                    101L, EVENT_ID, "Test Event", HALL_ID, "Main Hall",
                    END.plusHours(1), END.plusHours(3),
                    BigDecimal.valueOf(30), 50, null, null
            );
            when(eventSessionService.getAllByEvent(EVENT_ID)).thenReturn(List.of(sessionResponse, r2));

            mockMvc.perform(get("/api/ticket/events/{eventId}/sessions", EVENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(SESSION_ID))
                    .andExpect(jsonPath("$[1].id").value(101));
        }

        @Test
        @DisplayName("200 OK — boş list qaytarılır")
        void getAll_returnsEmptyList() throws Exception {
            when(eventSessionService.getAllByEvent(EVENT_ID)).thenReturn(List.of());

            mockMvc.perform(get("/api/ticket/events/{eventId}/sessions", EVENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("404 Not Found — event mövcud deyil")
        void getAll_returns404_whenEventNotFound() throws Exception {
            when(eventSessionService.getAllByEvent(EVENT_ID))
                    .thenThrow(new ResourceNotFoundException("Event tapılmadı"));

            mockMvc.perform(get("/api/ticket/events/{eventId}/sessions", EVENT_ID))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DELETE /api/ticket/events/{eventId}/sessions/{sessionId}
    // ─────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/ticket/events/{eventId}/sessions/{sessionId}")
    class DeleteEndpoint {

        @Test
        @DisplayName("204 No Content — uğurlu silmə")
        void delete_returns204() throws Exception {
            doNothing().when(eventSessionService).delete(EVENT_ID, SESSION_ID);

            mockMvc.perform(delete("/api/ticket/events/{eventId}/sessions/{sessionId}", EVENT_ID, SESSION_ID))
                    .andExpect(status().isNoContent());

            verify(eventSessionService).delete(EVENT_ID, SESSION_ID);
        }

        @Test
        @DisplayName("404 Not Found — session tapılmadı")
        void delete_returns404_whenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("EventSession tapılmadı"))
                    .when(eventSessionService).delete(EVENT_ID, SESSION_ID);

            mockMvc.perform(delete("/api/ticket/events/{eventId}/sessions/{sessionId}", EVENT_ID, SESSION_ID))
                    .andExpect(status().isNotFound());
        }
    }
}
