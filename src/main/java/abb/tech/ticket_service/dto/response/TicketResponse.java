package abb.tech.ticket_service.dto.response;

import abb.tech.ticket_service.enums.TicketStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketResponse {
    Long id;
    UUID ticketNumber;
    Long userId;
    Long eventSessionId;
    Long seatId;
    BigDecimal price;
    TicketStatus ticketStatus;
}
