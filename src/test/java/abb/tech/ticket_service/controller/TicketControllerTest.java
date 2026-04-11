package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.response.TicketResponse;
import abb.tech.ticket_service.exception.GlobalExceptionHandler;
import abb.tech.ticket_service.exception.ResourceNotFoundException;
import abb.tech.ticket_service.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final Long TICKET_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long ORDER_ID = 500L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Bilet Məlumatlarını Əldə Etmə Testləri")
    class GetTicketTests {

        @Test
        @DisplayName("GET /tickets/{id} — Uğurlu")
        void getTicketById_success() throws Exception {
            TicketResponse response = mock(TicketResponse.class);
            when(ticketService.getTicketById(TICKET_ID)).thenReturn(response);

            mockMvc.perform(get("/tickets/{id}", TICKET_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /tickets/user/{userId} — User-ə görə biletlər")
        void getTicketsByUserId_success() throws Exception {
            when(ticketService.getTicketsByUserId(USER_ID)).thenReturn(List.of(mock(TicketResponse.class)));

            mockMvc.perform(get("/tickets/user/{userId}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("GET /tickets/order/{orderId} — Sifarişə görə biletlər")
        void getTicketsByOrderId_success() throws Exception {
            when(ticketService.getTicketsByOrderId(ORDER_ID)).thenReturn(List.of(mock(TicketResponse.class)));

            mockMvc.perform(get("/tickets/order/{orderId}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("Bilet Ləğvi Testləri")
    class CancelTicketTests {

        @Test
        @DisplayName("DELETE /tickets/{id} — Uğurlu ləğv")
        void cancelTicket_success() throws Exception {
            doNothing().when(ticketService).cancelTicket(TICKET_ID);

            mockMvc.perform(delete("/tickets/{id}", TICKET_ID))
                    .andExpect(status().isNoContent());

            verify(ticketService).cancelTicket(TICKET_ID);
        }

        @Test
        @DisplayName("404 Not Found — ləğv ediləcək bilet tapılmadıqda")
        void cancelTicket_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Ticket tapılmadı"))
                    .when(ticketService).cancelTicket(TICKET_ID);

            mockMvc.perform(delete("/tickets/{id}", TICKET_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PDF Generasiyası Testləri")
    class PdfGenerationTests {

        @Test
        @DisplayName("GET /tickets/{id}/pdf — Uğurlu PDF generasiyası")
        void generateTicketPdf_success() throws Exception {
            byte[] mockPdf = "fake pdf content".getBytes();
            when(ticketService.generateTicketPdf(TICKET_ID)).thenReturn(mockPdf);

            mockMvc.perform(get("/tickets/{id}/pdf", TICKET_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ticket_" + TICKET_ID + ".pdf\""))
                    .andExpect(content().bytes(mockPdf));
        }
    }
}