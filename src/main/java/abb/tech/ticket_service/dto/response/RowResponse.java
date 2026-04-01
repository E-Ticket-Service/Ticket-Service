package abb.tech.ticket_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RowResponse {

    private Long id;
    private Integer rowNumber;
    private List<SeatResponse> seats;
}
