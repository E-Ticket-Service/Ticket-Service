package abb.tech.ticket_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueCreationRequest {

    @NotNull(message = "Venue name cannot be null")
    private String name;
    @NotNull(message = "Venue city cannot be null")
    private String city;
    @NotNull(message = "Venue address cannot be null")
    private String address;
    private String description;
}
