package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.SeatUpdateRequest;
import abb.tech.ticket_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PutMapping("/{id}")
    public void updateRow(@PathVariable Long id, @RequestBody SeatUpdateRequest request){
        seatService.updateSeat(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteRow(@PathVariable Long id){
        seatService.deleteSeat(id);
    }
}
