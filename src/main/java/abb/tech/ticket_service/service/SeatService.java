package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.SeatUpdateRequest;
import abb.tech.ticket_service.model.Seat;

public interface SeatService {

    Seat getById(Long id);
    void updateSeat(Long id, SeatUpdateRequest request);
    void deleteSeat(Long id);
    Seat save(Seat seat);
}
