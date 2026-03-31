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
public class HallCreationRequest {

    @NotNull(message = "Hall name cannot be null")
    private String name;
    @NotNull(message = "Hall capacity cannot be null")
    private Integer capacity;
    @NotNull(message = "Hall hasSeatMap cannot be null")
    private Boolean hasSeatMap;
    @Valid
    private List<SectionCreationRequest> sections;
}
