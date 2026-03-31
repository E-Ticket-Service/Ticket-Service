package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.BlockCreationRequest;
import abb.tech.ticket_service.dto.request.BlockUpdateRequest;
import abb.tech.ticket_service.model.Block;
import abb.tech.ticket_service.model.Section;

import java.util.List;

public interface BlockService {

    Block getById(Long id);
    void updateBlock(Long id, BlockUpdateRequest request);
    void deleteBlock(Long id);
}
