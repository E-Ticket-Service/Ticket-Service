package abb.tech.ticket_service.service;


import abb.tech.ticket_service.dto.request.RowCreationRequest;
import abb.tech.ticket_service.dto.request.RowUpdateRequest;
import abb.tech.ticket_service.model.Block;

import java.util.List;

public interface RowService {

    void createRow(RowCreationRequest request, Long blockId);
    void updateRow(Long id, RowUpdateRequest request);
    void deleteRow(Long id);
}
