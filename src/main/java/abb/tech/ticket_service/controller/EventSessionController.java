package abb.tech.ticket_service.controller;

import abb.tech.ticket_service.dto.request.ReqEventSessionDto;
import abb.tech.ticket_service.dto.response.RespEventSessionDto;
import abb.tech.ticket_service.service.EventSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket/events/{eventId}/sessions")
@RequiredArgsConstructor
public class EventSessionController {

    private final EventSessionService eventSessionService;
    /**
     * GET /api/events/{eventId}/sessions/{sessionId}
     * Tək EventSession əldə et
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<RespEventSessionDto> getById(
            @PathVariable Long eventId,
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(eventSessionService.getById(eventId, sessionId));
    }

    /**
     * GET /api/events/{eventId}/sessions
     * Event-ə aid bütün EventSession-ları əldə et
     */
    @GetMapping
    public ResponseEntity<List<RespEventSessionDto>> getAllByEvent(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(eventSessionService.getAllByEvent(eventId));
    }

    /**
     * POST /api/events/{eventId}/sessions
     * Yeni EventSession yarat
     */
    @PostMapping
    public ResponseEntity<RespEventSessionDto> create(
            @PathVariable Long eventId,
            @Valid @RequestBody ReqEventSessionDto request) {

        RespEventSessionDto response = eventSessionService.create(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/events/{eventId}/sessions/{sessionId}
     * Mövcud EventSession-u yenilə
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<RespEventSessionDto> update(
            @PathVariable Long eventId,
            @PathVariable Long sessionId,
            @Valid @RequestBody ReqEventSessionDto request) {

        return ResponseEntity.ok(eventSessionService.update(eventId, sessionId, request));
    }

    /**
     * DELETE /api/events/{eventId}/sessions/{sessionId}
     * EventSession-u sil
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long eventId,
            @PathVariable Long sessionId) {

        eventSessionService.delete(eventId, sessionId);
        return ResponseEntity.noContent().build();
    }
}
