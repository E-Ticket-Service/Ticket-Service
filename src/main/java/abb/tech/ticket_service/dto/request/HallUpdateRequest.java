package abb.tech.ticket_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HallUpdateRequest {

    private String name;
    private Integer capacity;
    private Boolean hasSeatMap;
}
