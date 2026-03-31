package abb.tech.ticket_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueCreationRequest {

    private String name;
    private String city;
    private String address;
    private String description;
    private List<HallCreationRequest> halls;
}
