package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.RowCreationRequest;
import abb.tech.ticket_service.dto.request.RowUpdateRequest;
import abb.tech.ticket_service.service.RowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rows")
@RequiredArgsConstructor
public class RowController {

    private final RowService rowService;

    @PostMapping("/{blockId}")
    public ResponseEntity<Void> createRow(@PathVariable Long blockId,
                                          @Valid @RequestBody RowCreationRequest request) {
        rowService.createRow(request, blockId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRow(@PathVariable Long id,
                                          @RequestBody RowUpdateRequest request) {
        rowService.updateRow(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRow(@PathVariable Long id) {
        rowService.deleteRow(id);
        return ResponseEntity.noContent().build();
    }
}
