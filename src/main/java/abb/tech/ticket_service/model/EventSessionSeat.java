package abb.tech.ticket_service.model;

import abb.tech.ticket_service.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "event_session_seats")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EventSessionSeat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_session_id")
    EventSession eventSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    Seat seat;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    SeatStatus seatStatus = SeatStatus.AVAILABLE;

    BigDecimal price;
}
