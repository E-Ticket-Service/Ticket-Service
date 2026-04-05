package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.SeatUpdateRequest;
import abb.tech.ticket_service.model.Seat;

import java.util.List;

public interface SeatService {

    Seat getById(Long id);
    List<Seat> findAllByHallId(Long hallId);
    Seat create(Seat seat);
    void updateSeat(Long id, SeatUpdateRequest request);
    void deleteSeat(Long id);
}
