package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.response.TicketResponse;
import abb.tech.ticket_service.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ticketService.getTicketsByOrderId(orderId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTicket(@PathVariable Long id) {
        ticketService.cancelTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateTicketPdf(@PathVariable Long id) {
        byte[] pdf = ticketService.generateTicketPdf(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ticket_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
