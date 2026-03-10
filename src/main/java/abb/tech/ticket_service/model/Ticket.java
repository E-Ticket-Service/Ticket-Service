package abb.tech.ticket_service.model;

import abb.tech.ticket_service.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Ticket extends BaseEntity{

    UUID ticketNumber;

    Long userId;

    @ManyToOne
    @JoinColumn(name = "event_session_id", referencedColumnName = "id")
    EventSession eventSession;

    @ManyToOne
    @JoinColumn(name = "seat_id", referencedColumnName = "id")
    Seat seat;

    BigDecimal price;

    @Enumerated(EnumType.STRING)
    TicketStatus ticketStatus;

}
