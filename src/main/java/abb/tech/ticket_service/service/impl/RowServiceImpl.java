package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.RowCreationRequest;
import abb.tech.ticket_service.dto.request.RowUpdateRequest;
import abb.tech.ticket_service.mapper.RowMapper;
import abb.tech.ticket_service.model.Block;
import abb.tech.ticket_service.model.Row;
import abb.tech.ticket_service.repository.RowRepository;
import abb.tech.ticket_service.service.BlockService;
import abb.tech.ticket_service.service.RowService;
import abb.tech.ticket_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RowServiceImpl implements RowService {

    private final RowRepository rowRepository;
    private final RowMapper rowMapper;
    private final BlockService blockService;

    public Row getById(Long id){
        return rowRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Row with id: %d not found", id)));
    }

    @Override
    @Transactional
    public void createRow(RowCreationRequest request, Long blockId) {
        var block = blockService.getById(blockId);
         var row = rowMapper.toEntity(request);
         row.setBlock(block);
         for(var seat: row.getSeats()){
             seat.setRow(row);
         }
         rowRepository.save(row);

    }

    @Override
    @Transactional
    public void updateRow(Long id, RowUpdateRequest request) {
        var row = getById(id);
        rowMapper.updateRow(row, request);
        rowRepository.save(row);
    }

    @Override
    public void deleteRow(Long id) {
        var row = getById(id);
        rowRepository.delete(row);
    }
}
