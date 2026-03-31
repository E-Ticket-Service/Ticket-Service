package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.BlockCreationRequest;
import abb.tech.ticket_service.dto.request.BlockUpdateRequest;
import abb.tech.ticket_service.mapper.BlockMapper;
import abb.tech.ticket_service.model.Block;
import abb.tech.ticket_service.model.Section;
import abb.tech.ticket_service.repository.BlockRepository;
import abb.tech.ticket_service.service.BlockService;
import abb.tech.ticket_service.service.RowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final BlockMapper blockMapper;
    private final RowService rowService;

    public Block getById(Long id){
        return blockRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Block with id: %d not found", id)));
    }

    @Override
    public void createBlocks(List<BlockCreationRequest> requests, Section section){
        for(var request: requests){
            var block = blockMapper.toEntity(request);
            block.setSection(section);
            blockRepository.save(block);

            if(request.getRows() != null && !request.getRows().isEmpty()){
                rowService.createRows(request.getRows(), block);
            }
        }
    }

    @Override
    public void updateBlock(Long id, BlockUpdateRequest request) {
        var block = getById(id);
        blockMapper.updateBlock(block, request);
        blockRepository.save(block);
    }

    @Override
    public void deleteBlock(Long id){
        var block = getById(id);
        blockRepository.delete(block);
    }
}
