package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.SeatUpdateRequest;

public interface SeatService {

    void updateSeat(Long id, SeatUpdateRequest request);
    void deleteSeat(Long id);
}
