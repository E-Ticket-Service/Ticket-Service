package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.RowCreationRequest;
import abb.tech.ticket_service.dto.request.RowUpdateRequest;
import abb.tech.ticket_service.service.RowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rows")
@RequiredArgsConstructor
public class RowController {

    private final RowService rowService;

    @PutMapping("/{blockId}")
    public void createRow(@PathVariable Long blockId, @Valid @RequestBody RowCreationRequest request){
        rowService.createRow(request, blockId);
    }

    @PutMapping("/{id}")
    public void updateRow(@PathVariable Long id, @RequestBody RowUpdateRequest request){
        rowService.updateRow(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteRow(@PathVariable Long id){
        rowService.deleteRow(id);
    }
}
