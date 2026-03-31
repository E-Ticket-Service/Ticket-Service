package abb.tech.ticket_service.service;

import abb.tech.ticket_service.dto.request.SeatCreationRequest;
import abb.tech.ticket_service.dto.request.SeatUpdateRequest;
import abb.tech.ticket_service.model.Row;

import java.util.List;

public interface SeatService {

    void createSeat(SeatCreationRequest request, Row row);
    void createSeats(List<SeatCreationRequest> requests, Row row);
    void updateSeat(Long id, SeatUpdateRequest request);
    void deleteSeat(Long id);
}
