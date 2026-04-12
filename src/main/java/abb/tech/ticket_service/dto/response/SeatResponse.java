package abb.tech.ticket_service.dto.response;

import abb.tech.ticket_service.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {

    private Long id;
    private Integer seatNumber;
    private SeatType seatType;
    private BigDecimal extraPrice;
}
