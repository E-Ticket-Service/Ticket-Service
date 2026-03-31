package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.BlockUpdateRequest;
import abb.tech.ticket_service.mapper.BlockMapper;
import abb.tech.ticket_service.model.Block;
import abb.tech.ticket_service.repository.BlockRepository;
import abb.tech.ticket_service.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final BlockMapper blockMapper;

    @Override
    public Block getById(Long id){
        return blockRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Block with id: %d not found", id)));
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
