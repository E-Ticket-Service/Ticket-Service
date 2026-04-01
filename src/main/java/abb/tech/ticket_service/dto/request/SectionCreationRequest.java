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
public class SectionCreationRequest {

    @NotNull(message = "Section name cannot be null")
    private String name;

    @Valid
    private List<BlockCreationRequest> blocks;
}
