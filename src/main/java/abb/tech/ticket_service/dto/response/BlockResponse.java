package abb.tech.ticket_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockResponse {

    private Long id;
    private String name;
    private List<RowResponse> rows;
}