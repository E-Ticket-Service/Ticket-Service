package abb.tech.ticket_service.dto.request;

import abb.tech.ticket_service.enums.SeatType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatCreationRequest {

    @NotNull(message = "Seat number cannot be null")
    private Integer seatNumber;
    private SeatType seatType;
    private BigDecimal extraPrice;
}
