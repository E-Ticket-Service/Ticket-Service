package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.response.TicketResponse;
import abb.tech.ticket_service.enums.TicketStatus;
import abb.tech.ticket_service.exception.NotFoundException;
import abb.tech.ticket_service.mapper.TicketMapper;
import abb.tech.ticket_service.model.Ticket;
import abb.tech.ticket_service.repository.TicketRepository;
import abb.tech.ticket_service.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketMapper ticketMapper;
    @Mock private PdfTicketService pdfTicketService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticket;
    private static final Long TICKET_ID = 1L;
    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
        ticket.setId(TICKET_ID);
        ticket.setUserId(USER_ID);
        ticket.setTicketStatus(TicketStatus.ACTIVE);
    }

    @Nested
    @DisplayName("Axtarış Testləri")
    class RetrievalTests {

        @Test
        @DisplayName("Uğurlu — ID-yə görə bilet tapılmalıdır")
        void getById_success() {
            when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));

            Ticket result = ticketService.getById(TICKET_ID);

            assertNotNull(result);
            assertEquals(TICKET_ID, result.getId());
        }

        @Test
        @DisplayName("Xəta — Bilet tapılmadıqda NotFoundException atmalıdır")
        void getById_notFound() {
            when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> ticketService.getById(TICKET_ID));
        }

        @Test
        @DisplayName("Uğurlu — Detallı bilet məlumatı tapılmalıdır")
        void getByIdWithDetails_success() {
            when(ticketRepository.findWithDetailsById(TICKET_ID)).thenReturn(Optional.of(ticket));

            Ticket result = ticketService.getByIdWithDetails(TICKET_ID);

            assertNotNull(result);
            verify(ticketRepository).findWithDetailsById(TICKET_ID);
        }
    }

    @Nested
    @DisplayName("Biznes Məntiqi Testləri")
    class BusinessLogicTests {

        @Test
        @DisplayName("Uğurlu — Bilet ləğv edilməli və statusu CANCELLED olmalıdır")
        void cancelTicket_success() {
            when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));

            ticketService.cancelTicket(TICKET_ID);

            assertEquals(TicketStatus.CANCELLED, ticket.getTicketStatus());
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Uğurlu — Bilet üçün PDF generasiya edilməlidir")
        void generateTicketPdf_success() {
            byte[] mockPdf = "PDF Content".getBytes();
            when(ticketRepository.findWithDetailsById(TICKET_ID)).thenReturn(Optional.of(ticket));
            when(pdfTicketService.generateTicketPdf(ticket)).thenReturn(mockPdf);

            byte[] result = ticketService.generateTicketPdf(TICKET_ID);

            assertArrayEquals(mockPdf, result);
            verify(pdfTicketService).generateTicketPdf(ticket);
        }
    }

    @Nested
    @DisplayName("Siyahı Testləri")
    class ListTests {

        @Test
        @DisplayName("Uğurlu — User ID-yə görə bilet siyahısı qaytarmalıdır")
        void getTicketsByUserId_success() {
            when(ticketRepository.findByUserId(USER_ID)).thenReturn(List.of(ticket));
            when(ticketMapper.toResponse(ticket)).thenReturn(TicketResponse.builder().build());

            List<TicketResponse> results = ticketService.getTicketsByUserId(USER_ID);

            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            verify(ticketRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("Uğurlu — Order ID-yə görə bilet siyahısı qaytarmalıdır")
        void getTicketsByOrderId_success() {
            Long orderId = 200L;
            when(ticketRepository.findByOrderId(orderId)).thenReturn(List.of(ticket));
            when(ticketMapper.toResponse(ticket)).thenReturn(TicketResponse.builder().build());

            List<TicketResponse> results = ticketService.getTicketsByOrderId(orderId);

            assertFalse(results.isEmpty());
            verify(ticketRepository).findByOrderId(orderId);
        }
    }
}