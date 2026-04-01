package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.BlockUpdateRequest;
import abb.tech.ticket_service.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBlock(@PathVariable Long id,
                                            @RequestBody BlockUpdateRequest request) {
        blockService.updateBlock(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlock(@PathVariable Long id) {
        blockService.deleteBlock(id);
        return ResponseEntity.noContent().build();
    }
}
