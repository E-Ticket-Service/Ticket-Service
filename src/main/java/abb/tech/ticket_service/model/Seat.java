package abb.tech.ticket_service.model;

import abb.tech.ticket_service.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "seats")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "row_id")
    Row row;

    Integer seatNumber;

    @Enumerated(EnumType.STRING)
    SeatType seatType;

    BigDecimal extraPrice;
}