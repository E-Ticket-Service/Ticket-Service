package abb.tech.ticket_service.dto.event;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketCreatedEvent {
    Long orderId;
    Long userId;
    String userEmail;
    List<TicketDetails> tickets;
    String status;

    @Data
    @Builder
    public static class TicketDetails {
        Long ticketId;
        String ticketNumber;
        String pdfBase64;
    }
}
