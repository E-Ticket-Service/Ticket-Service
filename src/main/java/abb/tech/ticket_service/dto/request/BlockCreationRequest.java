package abb.tech.ticket_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockCreationRequest {

    private String name;
    private List<RowCreationRequest> rows;
}
