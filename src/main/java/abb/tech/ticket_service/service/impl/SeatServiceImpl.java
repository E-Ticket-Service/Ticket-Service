package abb.tech.ticket_service.service.impl;

import abb.tech.ticket_service.dto.request.SeatCreationRequest;
import abb.tech.ticket_service.dto.request.SeatUpdateRequest;
import abb.tech.ticket_service.mapper.SeatMapper;
import abb.tech.ticket_service.model.Row;
import abb.tech.ticket_service.model.Seat;
import abb.tech.ticket_service.repository.SeatRepository;
import abb.tech.ticket_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;

    public Seat getById(Long id){
        return seatRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Seat with id: %d not found", id)));
    }

    @Override
    public void createSeat(SeatCreationRequest request, Row row) {
        var seat = seatMapper.toEntity(request);
        seat.setRow(row);
        seatRepository.save(seat);
    }

    @Override
    public void updateSeat(Long id, SeatUpdateRequest request){
        var seat = getById(id);
        seatMapper.updateSeat(seat, request);
        seatRepository.save(seat);
    }

    @Override
    public void deleteSeat(Long id){
        var seat = getById(id);
        seatRepository.delete(seat);
    }
}
