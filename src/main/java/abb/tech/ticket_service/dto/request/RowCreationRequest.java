package abb.tech.ticket_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RowCreationRequest {

    @NotNull(message = "Row number cannot be null")
    private Integer rowNumber;

    @Valid
    private List<SeatCreationRequest> seats;
}
