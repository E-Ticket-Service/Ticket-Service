package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.event.TicketCreatedEvent;
import abb.tech.ticket_service.model.*;
import abb.tech.ticket_service.service.impl.PdfTicketServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfTicketServiceImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private PdfTicketServiceImpl pdfTicketService;

    private Ticket ticket;
    private Order order;

    @BeforeEach
    void setUp() {
        Venue venue = new Venue();
        venue.setAddress("Baku, Azerbaijan");

        Hall hall = new Hall();
        hall.setName("Crystal Hall");
        hall.setVenue(venue);

        Event event = new Event();
        event.setName("Summer Festival");

        EventSession session = new EventSession();
        session.setEvent(event);
        session.setHall(hall);
        session.setStartTime(LocalDateTime.now());

        Seat seat = new Seat();
        seat.setSeatNumber(10);

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTicketNumber(UUID.randomUUID());
        ticket.setEventSession(session);
        ticket.setSeat(seat);
        ticket.setPrice(new java.math.BigDecimal("50.00"));

        order = new Order();
        order.setId(100L);
        order.setUserId(500L);
    }

    @Nested
    @DisplayName("generateTicketPdf Testləri")
    class PdfGenerationTests {

        @Test
        @DisplayName("Uğurlu — Bilet üçün PDF bayt massivi yaradılmalıdır")
        void generateTicketPdf_success() {
            byte[] result = pdfTicketService.generateTicketPdf(ticket);

            assertNotNull(result);
            assertTrue(result.length > 0);
            String pdfHeader = new String(result, 0, 5);
            assertEquals("%PDF-", pdfHeader);
        }

        @Test
        @DisplayName("Xəta — Məlumatlar çatışmadıqda xəta mesajı massivi qaytarmalıdır")
        void generateTicketPdf_errorHandling() {
            byte[] result = pdfTicketService.generateTicketPdf(null);

            assertNotNull(result);
            assertEquals("ERROR_GENERATING_PDF", new String(result));
        }
    }

    @Nested
    @DisplayName("generateAndSendTickets Testləri")
    class KafkaIntegrationTests {

        @Test
        @DisplayName("Uğurlu — PDF-lər yaradılmalı və Kafka-ya event göndərilməlidir")
        void generateAndSendTickets_success() throws Exception {
            List<Ticket> tickets = List.of(ticket);
            String userEmail = "ayla@test.com";

            pdfTicketService.generateAndSendTickets(tickets, userEmail, order);

            verify(kafkaTemplate).send(anyString(), eq(order.getId().toString()), anyString());

            verify(objectMapper).writeValueAsString(any(TicketCreatedEvent.class));
        }

        @Test
        @DisplayName("Xəta — Serialization zamanı xəta baş verərsə loglanmalı (exception atmamalı)")
        void generateAndSendTickets_serializationError() throws Exception {
            List<Ticket> tickets = List.of(ticket);
            when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON Error"));

            assertDoesNotThrow(() ->
                    pdfTicketService.generateAndSendTickets(tickets, "test@test.com", order)
            );
            verify(kafkaTemplate, never()).send(any(), any(), any());
        }
    }
}